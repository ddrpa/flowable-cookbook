# WIP flowable cookbook

本项目构想了一些特定需求的业务场景，通过配置 Flowable 流程来满足这些需求，并通过一个 SpringBoot 项目的单元测试来证明这些执行是正确的。

## 项目结构

项目整体基于 Spring Boot 3。

`bpmn/` 用于存放原始的流程定义文件，这些流程定义文件是使用 Visual Studio Code 插件 Miranum: Camunda Modeler 创建的，大部分情况下也可用于 Camunda。有关这一情况的详细信息，请参考 [ddrpa / camunda2flowable](https://github.com/ddrpa/camunda2flowable)。
