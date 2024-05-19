# 朋友圈

### 项目简介

朋友圈是一个社群管理平台，用户在此可以寻找志同道合的朋友

### 功能特点

- 用户注册：用户可以通过注册功能创建新账号
- 用户登录：用户可以凭账号登录，基于redis分布式存储session，实现单点登录
- 预热缓存：通过Spring Scheduler定时任务预加载缓存，通过Redisson实现分布式悲观锁
- 标签管理：用户可以自定义标签，可以给自己打算标签，根据标签搜索用户
- 队伍管理：用户可以加入队伍，创建队伍，退出队伍

### 技术栈

- Java
- Spring Boot
- Redis
- Mybatis-Plus
- Redisson
- Spring Scheduler

### 项目框架
![用户管理 drawio](https://github.com/asdrfdc/Friend-Circle/assets/163655764/ade61697-c812-44aa-983d-060a19d2efdf)
