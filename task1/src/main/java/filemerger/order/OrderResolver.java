package filemerger.order;

import java.util.List;

public interface OrderResolver {
    /**
     * Определяет порядок элементов
     * @param dependencies массив пар [from, to], где from должен идти после to
     * @return упорядоченный список элементов
     * @throws CyclicDependencyException если найден цикл
     */
    List<String> resolve(String[][] dependencies);
}
