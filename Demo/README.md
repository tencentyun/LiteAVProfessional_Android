### Tips

##### Q：如何使用已有SDK构建调试？

- 拷贝对应的aar文件到`LiteAVDemo/app/libs`目录，注意命名规则，尤其是`0.0.53`版本号之前是`-`；
- 在`LiteAVDemo/build.gradle`下，修改liteavSdk；

![](https://qcloudimg.tencent-cloud.cn/raw/995a8e98a807c4a00c38cd7585d330d9.png)

##### Q：如何配置TRTC版本的编译

- 修改`LiteAVDemo/gradle.properties`

````
# 配置不同版本的编译
# 注意：重构版本目前仅支持TRTC、Live；
# 临时增加重构Live版本参数：buildRefactoringLive，待Live重构接口对齐后，删除此变量，勿做他用
buildLive=false
buildTRTC=true
buildUGC=false
buildPlayer=false
buildSmart=false
buildInternational=false
buildRefactoringLive=false
````

- 点击运行即可

##### Q：如果我想进行SDK源码联合调试怎么办

- 参见`LiteAVDemo/gradle.properties`，将`liteavSourcePath`修改为你liteav代码对应的目录即可；

  ```
  # SDK 源码调试配置（二者只能选择配置一个）
  # SDK 线上版本源码编译调试的配置
  # 示例：liteavSourcePath=/Users/parkhuang/src/liteav
  liteavSourcePath=/Users/parkhuang/src/liteav
  ```

- 在`LiteAVDemo/local.properties`文件中增加ndk配置，此处建议为r16b；

  ```
  sdk.dir=/Users/tatemin/Library/Android/sdk
  ndk.dir=/Users/tatemin/Library/Android/sdk/ndk/r16b
  ```

### 备注说明

目前LiteAVDemo需要满足如下诉求：主线企业版、专业版、TRTC等多个版本的Demo构建、重构TRTC、Live等版本的Demo构建；同时针对各个版本不止有功能的差异，还有诸如强制升级、Bugly上报等差异，复杂性较高，另外部分逻辑因为历史原因正在逐步修改，大家在开发过程中如果有疑惑的地方可以随时联系tatemin；



### 