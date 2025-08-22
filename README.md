### Java_Vedio
这是一个简单的视频播放网站，由springboot+redis+mysql+rocketmq来构建服务（项目的名称vedio拼写打错，正确的应该是video）。

#### 环境安装
redis是作为记录分片上传的索引，以实现断点续传的功能；记录上传任务的开始，设置过期时间，时间到会通知系统删除过期分片

mysql是作为成功上传的记录，当再次上传时，如果在数据库检索出相同的哈希，则直接提示上传成功（秒传）。

rocketmq是作为服务解耦的消息队列，把项目拆成两个服务，一个uplaod，负责上传分片的保存、过期的清理、全部视频的获取和返回播放，一个process，负责分片的合并、视频封面的生成、.m3u8格式的转换、合并完成的分片处理；

ffmpeg是安装在springboot项目运行的环境中，在windows运行项目就在windows装ffmpeg，在linux运行就在linux安装ffmpeg。

redis、mysql、rocketmq都是采用docker容器，利用docker-compose来管理

安装docker和docker-compose后要进行换源，在/etc/docker/daemon.json中写入：
```json
{
    "registry-mirrors": [
        "https://docker.m.daocloud.io",
        "https://docker.imgdb.de",
        "https://docker-0.unsee.tech",
        "https://docker.hlmirror.com",
        "https://docker.1ms.run",
        "https://func.ink",
        "https://lispy.org",
        "https://docker.xiaogenban1993.com"
    ]
}
```
或者在网上找其他的镜像源

在启动镜像前，要先做一点mysql和redis的配置

```bash
mkdir /root/docker/mysql_data/vedio
mkdir /root/docker/redis/conf
mkdir /root/docker/redis/data
cd /root/docker/redis/conf
touch redis.conf
echo "protected-mode no" >redis.conf
echo "appendonly yes" >>redis.conf
echo "requirepass 123456" >> redis.conf
cd /root/
```

docker-compose.yaml:
```yaml
version: '3.8'

services:
  namesrv:
    image: apache/rocketmq:4.9.6
    container_name: rmqnamesrv
    ports:
      - 9876:9876
    networks:
      - rocketmq
    command: sh mqnamesrv

  broker:
    image: apache/rocketmq:4.9.6
    container_name: rmqbroker
    ports:
      - 10909:10909
      - 10911:10911
      - 10912:10912
    environment:
      - NAMESRV_ADDR=rmqnamesrv:9876
    volumes:
      - ./broker.conf:/home/rocketmq/rocketmq-4.9.6/conf/broker.conf
    depends_on:
      - namesrv
    networks:
      - rocketmq
    command: sh mqbroker -c /home/rocketmq/rocketmq-4.9.6/conf/broker.conf

  vedio_mysql:
    container_name: vedio_mysql
    image: mysql:8.0.43
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      TZ: Asia/Shanghai
      MYSQL_DATABASE: vedio_mysql # 自动创建数据库
      MYSQL_CHARSET: utf8mb4
    volumes:
      - /root/docker/mysql_data/vedio:/var/lib/mysql  # 数据持久化
      - /etc/localtime:/etc/localtime
      - /root/docker/init.sql:/docker-entrypoint-initdb.d/init.sql  # 挂载初始化脚本
    ports:
      - "3306:3306"
    networks:
      - default
    healthcheck: # 健康检查
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 3s
      retries: 5
  vedio_redis:
    container_name: vedio_redis
    image: redis:latest
    volumes:
      - /root/docker/redis/data:/data  # 数据持久化
      - /root/docker/redis/conf/redis.conf:/etc/redis/redis.conf
    ports:
      - "6379:6379"
    restart: always
    networks:
      - default
    command: ["redis-server","/etc/redis/redis.conf"]

networks:
  rocketmq:
    driver: bridge
```
通过`docker-compose -f docker-compose.yaml up -d`会自动去仓库拉取对应的镜像

上述命令可能不一定正确，只需要镜像可以启动即可，镜像版本不一定要绝对正确，mysql是8.0以上就好

mybatis-plus使得查询和插入mysql变得很简单，很多都不需要写代码。

#### 系统启动
vscode打开项目，打开java_upload的App.java文件，点击run就可以启动java_upload，java_process同理。

效果：

<img src="https://youke1.picui.cn/s1/2025/08/22/68a830e5ba2ae.png" width=500px>

<img src="https://youke1.picui.cn/s1/2025/08/22/68a830e62fdde.png" width=500px>

前端的计算哈希如果视频文件太大(可能是100MB)，就会导致缓冲区溢出。
