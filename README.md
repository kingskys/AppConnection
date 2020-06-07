# AppConnection
**多个 Android App 间通讯 使用Messenger实现**

使用示例
--------
- 在Application里面初始化
<pre>
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        ConnClient.init("001", "com.kingskys.appconnection", this, new ConnClientListener());
    }
</pre>
  - 参数1. 客户端ID，每个单独设置。例如客户端1 "001"，客户端2 "002"。
  - 参数2. 服务器包名。哪个包作为服务器就用哪个包的包名，但是必须是引入本模块的。
  - 参数3. 消息回调。具体看ConnClientListener代码。
  
- 发送数据
  <pre>
   ConnClient.waitOnlineOk(); // 等待连接服务器成功
   ConnClient.send("发送的消息内容");
  </pre>
  #### 注意：发送消息必须在分线程
 
gradle引入
---------
<pre>
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    ...
    implementation 'com.github.kingskys:AppConnection:master-SNAPSHOT'
}
</pre>
