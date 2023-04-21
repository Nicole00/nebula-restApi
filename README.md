# nebula-restAPI

新增加执行接口，只需改动`com.vesoft.nebula.graph.server.controller.GraphController`类

新接口内需要做两件事：
 1. 构造查询语句
 2. 调用execute(statement)接口。

