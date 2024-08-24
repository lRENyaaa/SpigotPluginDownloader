# SpigotPluginDownloader
一个实验性的SpigotMC插件下载器，基于Java+Electron

## 为什么要Electron?
SpigotMC下载要过人机验证，对于我来说使用纯Java过Cloudflare的人机验证几乎是不可能的
## 使用方式
clone后gradlew build即可完成编译  
运行时需依赖npm进行electron应用的现场编译和运行  
启动命令:
``` shell
java -jar SpigotPluginDownloader-1.0-SNAPSHOT-all.jar <PluginID>
```