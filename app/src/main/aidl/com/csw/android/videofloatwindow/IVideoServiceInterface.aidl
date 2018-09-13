// IVideoServiceInterface.aidl
package com.csw.android.videofloatwindow;

// Declare any non-default types here with import statements

interface IVideoServiceInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    //aidl 是进程间通讯语言，定义的接口可以在服务端和客户端获得实例，定义方法时，参数和返回值必须能序列化

    /**
    * 显示浮动窗口
    */
    void playInFloatWindow();
}
