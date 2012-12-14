package utils;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public class Util {
    public interface Transformer<E, T> {
        T transform(E object);  
    }
    
    public static <E, T> List<T> transformList(final List<E> list, final Transformer<E, T> transformer) {
        if (list == null) {
            return null;
        }
        if (list instanceof RandomAccess) {
            return new AbstractList<T>() {
                @Override
                public T get(int index) {
                    return transformer.transform(list.get(index));
                }

                @Override
                public int size() {
                    return list.size();
                }
            };
        } else {
            return new AbstractSequentialList<T>() {

                @Override
                public int size() {
                    return list.size();
                }

                @Override
                public ListIterator<T> listIterator(final int index) {
                    
                    return new ListIterator<T>() {
                        
                        ListIterator<E> listIterator = list.listIterator(index);

                        @Override
                        public void add(T element) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public boolean hasNext() {
                            return listIterator.hasNext();
                        }

                        @Override
                        public boolean hasPrevious() {
                            return listIterator.hasPrevious();
                        }

                        @Override
                        public T next() {
                            return transformer.transform(listIterator.next());
                        }

                        @Override
                        public int nextIndex() {
                            return listIterator.nextIndex();
                        }

                        @Override
                        public T previous() {
                            return transformer.transform(listIterator.previous());
                        }

                        @Override
                        public int previousIndex() {
                            return listIterator.previousIndex();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void set(T element) {
                            throw new UnsupportedOperationException();
                        }
                        
                    };
                }
            };
        }
    }
}
