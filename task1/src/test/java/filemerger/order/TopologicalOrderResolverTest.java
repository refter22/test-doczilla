package filemerger.order;

class TopologicalOrderResolverTest extends OrderResolverTest {
    @Override
    OrderResolver getResolver() {
        return new TopologicalOrderResolver();
    }
}