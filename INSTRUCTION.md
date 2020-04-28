## 課題

この課題をこなすことがトレーニングのゴールです。
独力でも手を動かしながら進められるようようになっていますが、可能ならトレーナーと対話しながらすすめることでより効果的に学べます。

## この課題で身につく能力

- Akka HTTPと、別プロセスになっているアクターを用いたバックエンドとのAkkaクラスタリングによる接続
- CQRSとの統合
- Akka HTTPとAkkaバックエンドそれぞれを複数プロセス立ち上げスケールさせる
- Akkaクラスタリングアプリケーション全体のパフォーマンスを測定する

### 事前準備:

- MySQL8.0.19をローカル開発環境にインストールしてください
  - `brew update`
  - `brew install mysql@8.0.19`
  - `mysql.Sever stop` //もし自分の環境で別のバージョンのMySQLが走っていたら
  - `/usr/local/opt/mysql@8.0/bin/mysql.Sever start`
- Mavenをインストールしてください
  - `brew install maven`

### 作業開始:

- このレポジトリをgit cloneしてください
  - `git clone git@github.com:mvrck-inc/training-akka-java-4-cqrs.git`

この課題はgit clone下ソースコードをそのまま使うことで、自分で新たにソースコードを書くことなく実行できるようになっています。
もちろん、自分で書き方を覚えたい方や、最後の発展的内容に取り組みたい方はご自分でぜひソースコードを書いてみてください。

---
- データベースのセットアップをしてください ([setup.sql](./dbsetup/setup.sql)) 
  - 参考: akka-persistence-jdbcプラグインのデフォルト・テーブル構成([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/schema/mysql/mysql-schema.sql))

今回のトレーニングでは`snapshot`テーブルは利用しないので、そちらは無視します。

イベント・ソーシングで使うための上記2つのテーブルに加え、CQRSのRead側で使うための以下の2つのテーブルを定義します。

`SELECT * FROM ticket_stocks;`:

| ticket_id | quantity | 
|-----------|----------|


`SELECT * FROM orders;`:

| id | tikcet_id | user_id | quantity |
|----|-----------|---------|----------|

---
- backendプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.BackendMain`
- akka-httpプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.HttpMain`
- read-sideプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.ReadSideMain`

TODO: - 画像、3プロセスの役割分割

第4回のトレーニングでは`Main`と`ReadSideMain`の2つのプロセスに分割しましたが、今回は3つのプロセスに分割します。
`HttpMain`は自身で`TicketStockActor`と`OrderActor`を保持せず、クラスタリングの[`ShardRegion Proxy`](https://doc.akka.io/docs/akka/2.5.4/scala/cluster-sharding.html#proxy-only-mode)を通して`BackendMain`にアクターの保持を移譲します。

`BackendMain`を最初に立ち上げてください。これは`BackendMain`がシードノードに属するからです。クラスタを構成するノードは、[`シードノード`](https://doc.akka.io/docs/akka/current/typed/cluster-concepts.html#seed-nodes)と呼ばれる一群と、非シードノードに分かれます。シードノードを最初に立ち上げる必要があり、非シードノードはシードノードが構成した初期クラスタにジョインします。
このトレーニングでは`BackendMain`が単一シードノードとして初期クラスタを構成し、`HttpMain`と`ReadSideMain`が非シードノードです。

- `BackendMain`: IPアドレス`127.0.0.1` ポート`2551`、シードノード

```
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< org.mvrck.training:akka-java-5-clustering >--------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-5-clustering ---
SL[2020-04-28 09:48:58,395] [INFO] [akka.event.slf4j.Slf4jLogger] [] [MaverickTraining-akka.actor.default-dispatcher-3] - Slf4jLogFger started {}
4J: A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
TicketStockActor created for PersistenceId(TicketStockActor|2)
TicketStockActor created for PersistenceId(TicketStockActor|1)
```

- `HttpMain`: IPアドレス`127.0.0.1` ポート`2552`

```
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< org.mvrck.training:akka-java-5-clustering >--------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-5-clustering ---
SLF4J[2020-04-28 09:49:19,947] [INFO] [akka.event.slf4j.Slf4jLogger] [] [MaverickTraining-akka.actor.default-dispatcher-3] - Slf4jLog:ger started {}
 A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
Server online at http://localhost:8080/
Press RETURN to stop...
```

- `ReadSideMain`: IPアドレス`127.0.0.1` ポート`2553`

```
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< org.mvrck.training:akka-java-5-clustering >--------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-5-clustering ---
SLF4J:[2020-04-28 11:26:03,713] [INFO] [akka.event.slf4j.Slf4jLogger] [] [MaverickTraining-akka.actor.default-dispatcher-3] - Slf4jLog ger started {}
A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
TicketStockCreated: 1, 5000
TicketStockCreated: 2, 2000
```

---
- curlでデータを挿入してください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 2, \"quantity\": 1}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください
  - サーバー側ログを確認してください
  - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql))

`BackendMain`プロセスのログを確認するとこのようなログが出力されているはずです。つまり`HttpMain`プロセスがHTTP POSTリクエストを受け付けた後、アクター側の処理は`BackendMain`プロセス内で行われます。

```
TicketStockActor created for PersistenceId(TicketStockActor|1)
OrderActor created for PersistenceId(OrderActor|42bcebe6-db2d-4fc0-91c3-8f88ace0be3c)
```

もう2回同じ`curl`を実行するとこのようになります。`TicketStockActor created for ...`が表示されるのは初回の`curl`のみで、2回目以降は`OrderActor created for ...`飲みが表示されます。

```
OrderActor created for PersistenceId(OrderActor|94acb6e7-30bd-4351-b7df-27dec084b398)
OrderActor created for PersistenceId(OrderActor|bb3ab139-d1dc-4deb-a22e-e226e63b7d50)
```


---
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側の実行結果を確認してください
    - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql)) 

不思議ですが、私のローカル環境では第4回のトレーニングより若干性能が上がってしまいました。

```
Running 5s test @ http://localhost:8080/orders
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    39.06ms   12.74ms 107.37ms   76.05%
    Req/Sec    51.37     12.63    80.00     78.00%
  516 requests in 5.02s, 94.73KB read
Requests/sec:    102.69
Transfer/sec:     18.85KB
```

---
- akka-clusteringのセットアップを確認してください
  - [pom.xml](./pom.xml#L31L35)
  - [application.conf](./src/main/resources/application.conf)
  - [http-main.conf](./src/main/resources/application.conf)
  - [backend-main.conf](./src/main/resources/application.conf)
  - [readside-main.conf](./src/main/resources/application.conf)
  
https://github.com/akka/akka/blob/master/akka-cluster/src/main/resources/reference.conf#L67-L74
https://doc.akka.io/docs/akka/current/typed/cluster.html#node-roles

https://github.com/akka/akka/blob/v2.6.4/akka-cluster-sharding/src/main/resources/reference.conf#L17-L19
> Cluster sharding init should be called on every node for each entity type. Which nodes entity actors are created on can be controlled with roles. init will create a ShardRegion or a proxy depending on whether the node’s role matches the entity’s role.
withRole


  /**
   * INTERNAL API
   * If true, this node should run the shard region, otherwise just a shard proxy should started on this node.
   * It's checking if the `role` and `dataCenter` are matching.
   */
  @InternalApi
  private[akka] def shouldHostShard(cluster: Cluster): Boolean =
    role.forall(cluster.selfMember.roles.contains) &&
    dataCenter.forall(_ == cluster.selfMember.dataCenter)


    
http-mainの中身を確かめてください - clustersharding, shardregion
entitykeyを確かめてください
backend-main
- akka-clusteringのセットアップを確認してください
  - [pom.xml](./pom.xml#L31-35L)
  - [application.conf](./src/main/resources/application.conf),

`application.conf`の中で、`HttpMain`と`BackendMain`がJVMをまたいでやり取りするために、以下のシリアライザ設定が加えられています。

```  
akka {
    ...

actor {
  provider = "cluster"
  serialization-bindings {
    ...
    "org.mvrck.training.actor.TicketStockActor$Command" = jackson-json
    "org.mvrck.training.actor.OrderActor$Command" = jackson-json
    "org.mvrck.training.actor.OrderActor$Response" = jackson-json
  }
}
```

- [http-main.conf](./src/main/resources/application.conf), [backend-main.conf](./src/main/resources/application.conf),  [readside-main.conf](./src/main/resources/application.conf)

上記3つのファイルにはホストのIPアドレスとポートが設定されています。本番環境ではIPアドレスは環境変数から与えられ、ポートは固定になることが多いでしょう。

```
akka {
  remote.artery {
    canonical {
      hostname = "127.0.0.1"
      # もしくは2552, 2553
      port = 2551
    }
  }
}
```

また`akka.cluster.roles`も設定されています。これもクラスタリング機能の利用には必要です。

```
akka {
  cluster {
    # もしくは ["http"], ["readside"]
    roles = ["backend"]
  }
}
```

上記の`akka.cluster.roles`と[`HttpMain`](./src/main/java/org/mvrck/training/app/HttpMain.scala)、[`BackendMain`](./src/main/java/org/mvrck/training/app/BackendMain.scala)での
`withRole`の利用によって「`OrderActor`と`TicketStockActor`の両エンティティは`backend`ロールを持つノードでのみ保持する」、つまり`BackendMain`のプロセス内でのみアクターを保持するという制約を設けています。

```
sharding.init(Entity.of(OrderActor.ENTITY_TYPE_KEY, ctx -> OrderActor.create(ctx.getEntityId())).withRole("backend"));
sharding.init(Entity.of(TicketStockActor.ENTITY_TYPE_KEY, ctx -> TicketStockActor.create(sharding, ctx.getEntityId())).withRole("backend"));
```

- [`akka.cluster.roles`](https://github.com/akka/akka/blob/master/akka-cluster/src/main/resources/reference.conf#L67-L74)
- [Node Roles](https://doc.akka.io/docs/akka/current/typed/cluster.html#node-roles)
- [`akka.cluster.sharding.role`](https://github.com/akka/akka/blob/v2.6.4/akka-cluster-sharding/src/main/resources/reference.conf#L17-L19)
  - `Entity.of(...)`でエンティティの生成方法を指定する時`.withRole()`を使わないと、`akka.cluster.sharding.role`で指定した値を利用することになる
  - 本番環境ではEntityごとに細かくroleを切り替えたい場合が多いと思うので`withRole`は必須くらいに思っておいていいのでは？

### 発展的内容:

- トレーニング1で考えたよう多数のテーブルを作成した場合、シーケンス図を書いてアクターからコマンド側の永続化層、クエリ側の永続仮想へと続く処理を整理してください
