package plugin.dryseed.chapter1.dynamic_proxy_hook.dynamic_proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author weishu
 * @date 16/1/28
 */
public class TestDynamic {
    public static void main(String[] args) {
        Shopping women = new ShoppingImpl();

        // 正常购物
        System.out.println(Arrays.toString(women.doShopping(100)));

        // 招代理
        women = (Shopping) Proxy.newProxyInstance(
                Shopping.class.getClassLoader(),    // ClassLoader loader
                women.getClass().getInterfaces(),   // Class<?>[] interfaces
                new ShoppingHandler(women)          // InvocationHandler h
        );

        System.out.println(Arrays.toString(women.doShopping(100)));
    }
}
