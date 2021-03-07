# Docker image for springboot application


### 基础镜像，使用alpine操作系统，openjkd使用8u201
FROM openjdk:8u201-jdk-alpine3.9
# 修改时区
RUN apk add -U tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#作者
MAINTAINER zbj <pts2020@163.com>

#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

#声明一个挂载点，容器内此路径会对应宿主机的某个文件夹
#VOLUME /tmp

#应用构建成功后的jar文件被复制到镜像内，名字也改成了app.jar
ADD target/datag-0.2.0.jar test.jar

#启动容器时的进程
ENTRYPOINT ["java","-jar","/test.jar"]

#暴露8080端口
EXPOSE 8600