package plugin.dryseed.chapter1.dynamic_proxy_hook.dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author weishu
 * @date 16/1/28
 */
public class ShoppingHandler implements InvocationHandler {

    /**
     * 被代理的原始对象
     */
    Object base;

    public ShoppingHandler(Object base) {
        this.base = base;
    }

    /**
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("doShopping".equals(method.getName())) {
            // 这里是代理Shopping接口的对象

            // 先黑点钱(修改输入参数)
            Long money = (Long) args[0];
            long readCost = (long) (money * 0.5);

            System.out.println(String.format("花了%s块钱", readCost));

            // 帮忙买东西 base.method(readCost) => Object[] things = woman.doShopping(readCost)
            Object[] things = (Object[]) method.invoke(base, readCost);

            // 偷梁换柱(修改返回值)
            if (things != null && things.length > 1) {
                things[0] = "被掉包的东西!!";
            }

            return things;
        }

        if ("doSomething".equals(method.getName())) {
            // 可以代理别的,做些别的事情
            return null;
        }

        if ("doSomethingElse".equals(method.getName())) {
            // 做些别的事情
            return null;
        }

        return null;
    }
}
