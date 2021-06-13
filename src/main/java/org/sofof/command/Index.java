/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sofof.BindingNamesTree;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;
import org.sofof.SequentialReader;
import org.sofof.SofofException;
import org.sofof.annotation.NonRelational;
import org.sofof.command.condition.ObjectCondition;

/**
 * Index data according to a specific key. This command can be executed to index
 * data for first time then further modification on the data will conduct
 * automatic indexing to it. When queried this command can extract objects as
 * fast as possible.
 *
 * @author Rami Manaf Abdullah
 */
public class Index implements Executable, Query, Serializable {

    private static final long serialVersionUID = 542325558;
    static final String SOFOF_INDEX = "SofofIndex";

    private String bindingName;
    private Class clazz;
    private String indexExpression;
    private List<? extends Comparable> keys;

    private Index() {
    }

    /**
     * used to index data for the first time based on the key. only use as
     * executable
     *
     * @param clazz
     * @param indexExpression an expression to extract the key from objects
     */
    public Index(Class clazz, String indexExpression) {
        this.clazz = clazz;
        this.indexExpression = Objects.requireNonNull(indexExpression);
    }

    /**
     * used to search about objects using the keys. only use as query
     *
     * @param clazz
     * @param keys
     */
    public Index(Class clazz, List<Comparable> keys) {
        this.clazz = clazz;
        this.keys = Objects.requireNonNull(keys);
    }

    /**
     * Specify the binding name that objects are bound to. If the name is empty
     * space filled strings or null then the name will be converted to
     * SofofNoName
     *
     * @param bind binding name
     * @return this object
     */
    public Index from(String bind) {
        this.bindingName = bind;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        bindingName = BindingNamesTree.parseNoName(bindingName);
        try ( SequentialReader reader = in.createSequentialReader(bindingName, clazz)) {
            Object obj;
            Indexes indexes = new Indexes(bindingName, clazz);
            int i = 0;
            while ((obj = reader.read()) != null) {
                Object result = ExpressionExecuter.execute(indexExpression, obj);
                checkIndexCompatablity(result, indexes);
                indexes.put((Comparable) result, i);
                i++;
            }
            new Bind(indexes).to(SOFOF_INDEX).execute(in, out);
            in.getBindingNamesTree().getBindingName(bindingName).getBindingClass(clazz).setIndexExpression(indexExpression);
            return i;
        } catch (Exception ex) {
            throw new SofofException(ex);
        }
    }

    static void checkIndexCompatablity(Object obj, Indexes indexes) throws SofofException {
        if (obj == null) {
            throw new SofofException("indexing key cann't be null");
        } else if (!Comparable.class.isAssignableFrom(obj.getClass())) {
            throw new SofofException("indexing key must be comparable");
        } else if (indexes.contains((Comparable) obj)) {
            throw new SofofException("indexing key must be unique");
        }
    }

    static Indexes getIndexes(String bindingName, Class clazz, ListInputStream in) throws SofofException {
        return (Indexes) new Select(Indexes.class).from(SOFOF_INDEX).where(new ObjectCondition("#getBindingName()", Operation.Equal, bindingName).and(new ObjectCondition("#getClazz()", Operation.Equal, clazz))).query(in).get(0);
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        bindingName = BindingNamesTree.parseNoName(bindingName);
        Indexes indexes = getIndexes(bindingName, clazz, in);
        if (indexes == null) {
            throw new SofofException("trying to search in unindexed data");
        }
        ArrayList<Integer> resultsIndexes = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            resultsIndexes.add((Integer) indexes.get(keys.get(i)));
        }
        if (resultsIndexes.isEmpty()) {
            return resultsIndexes;
        }
        int max = Collections.max(resultsIndexes);
        try ( SequentialReader reader = in.createSequentialReader(bindingName, clazz)) {
            int index = 0;
            int indexOfIndex;
            ArrayList results = new ArrayList(Collections.nCopies(resultsIndexes.size(), null));
            while (index <= max) {
                if ((indexOfIndex = resultsIndexes.indexOf(index)) > -1) {
                    results.set(indexOfIndex, reader.read());
                } else {
                    reader.skip();
                }
                index++;
            }

            return results;
        } catch (Exception ex) {
            throw new SofofException(ex);
        }
    }

    @NonRelational
    static class Indexes<Key extends Comparable<Key>> implements Serializable {

        private static final long serialVersionUID = 96643239;

        private String bindingName;
        private Class clazz;
        private ArrayList<Entry<Key>> data = new ArrayList<>();

        private Indexes() {
        }

        public Indexes(String bindingName, Class clazz) {
            this.bindingName = bindingName;
            this.clazz = clazz;
        }

        public String getBindingName() {
            return bindingName;
        }

        public void setBindingName(String bindingName) {
            this.bindingName = bindingName;
        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }

        public void put(Key key, int value) {
            Entry entry = new Entry(key, value);
            data.add(-(Collections.binarySearch(data, entry) + 1), entry);
        }

        public void update(Key oldKey, Key newKey) {
            int oldKeyIndex = Collections.binarySearch(data, new Entry(oldKey));
            Entry entry = data.get(oldKeyIndex);
            data.remove(oldKeyIndex);
            put(newKey, entry.getIndex());
        }

        public boolean contains(Key key) {
            int index = Collections.binarySearch(data, new Entry(key));
            if (index < 0 || index == data.size() || !data.get(index).getKey().equals(key)) {
                return false;
            }
            return true;
        }

        public int get(Key key) {
            return data.get(Collections.binarySearch(data, new Entry(key, 0))).getIndex();
        }

        public Entry get(int index) {
            return data.get(index);
        }

        public void remove(Key key) {
            int index = data.remove(Collections.binarySearch(data, new Entry(key))).getIndex();
            data.stream().filter((t) -> t.getIndex() > index).forEach((Entry t) -> t.shiftDown());
        }

        public int size() {
            return data.size();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Indexes<?> other = (Indexes<?>) obj;
            if (!Objects.equals(this.bindingName, other.bindingName)) {
                return false;
            }
            if (!Objects.equals(this.clazz, other.clazz)) {
                return false;
            }
            return true;
        }

        static class Entry<Key extends Comparable> implements Comparable<Entry>, Serializable {

            private static final long serialVersionUID = 64665766;

            private Key key;
            private Integer index;

            public Entry() {
                this(null, 0);
            }

            public Entry(Key key) {
                this(key, 0);
            }

            public Entry(Key key, int index) {
                this.key = key;
                this.index = index;
            }

            public Key getKey() {
                return key;
            }

            public void setKey(Key key) {
                this.key = key;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }

            public void shiftDown() {
                this.index--;
            }

            @Override
            public int compareTo(Entry o) {
                return key.compareTo(o.key);
            }

            @Override
            public int hashCode() {
                int hash = 3;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Entry other = (Entry) obj;
                if (!Objects.equals(this.key, other.key)) {
                    return false;
                }
                return true;
            }

        }

    }
}
