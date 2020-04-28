JavaによるAkkaトレーニング第5回 

## Akkaクラスタリング

いよいよAkkaクラスタリング機能の紹介です。
アクター、イベントソーシングとCQRS、そしてクラスタリングを用いることでAkkaの力を最大限活かすことが出来る、というのが私の考えです。
ありとあらゆるシステムでこれら全ての機能を使う必要はありませんが、やはりAkkaの醍醐味を味わうことが出来るのはアクターをクラスタ上で水平スケールさせて、
イベント・ソーシングとCQRSで対障害性と疎結合を実現する場合です。

このトレーニングではCQRS - Command Query Responsibility Separationと呼ばれる設計パターンの中を使ってイベント・ソーシングでは重視しなかったデータ読み込み側の処理を補完します。

- [第1回のトレーニング: リレーショナル・データベースのトランザクションによる排他制御](https://github.com/mvrck-inc/training-akka-java-1-preparation)
- [第2回のトレーニング: アクターによる非同期処理](https://github.com/mvrck-inc/training-akka-java-2-actor)
- [第3回のトレーニング: アクターとデータベースのシステム(イベント・ソーシング)](https://github.com/mvrck-inc/training-akka-java-3-persistence)
- [第4回のトレーニング: アクターとデータベースのシステム(CQRS)](https://github.com/mvrck-inc/training-akka-java-4-cqrs)
- [第5回のトレーニング: クラスタリング](https://github.com/mvrck-inc/training-akka-java-5-clustering)

## 課題

この課題をこなすことがトレーニングのゴールです。
独力でも手を動かしながら進められるようようになっていますが、可能ならトレーナーと対話しながらすすめることでより効果的に学べます。

## この課題で身につく能力

- Akka HTTPと、別プロセスになっているアクターを用いたバックエンドとのAkkaクラスタリングによる接続
- CQRSとの統合
- Akka HTTPとAkkaバックエンドそれぞれを複数プロセス立ち上げスケールさせる
- Akkaクラスタリングアプリケーション全体のパフォーマンスを測定する

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
- データベースのセットアップをしてください ([setup.sql](./dbsetup/setup.sql)) 
  - 参考: akka-persistence-jdbcプラグインのデフォルト・テーブル構成([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/schema/mysql/mysql-schema.sql))
- backendプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.BackendMain`
- akka-httpプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.HttpMain`
- read-sideプロセスを起動してください `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.ReadSideMain`
- curlでデータを挿入してください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 2, \"quantity\": 1}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください
  - サーバー側ログを確認してください
  - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql))
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側の実行結果を確認してください
    - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql)) 
- akka-clusteringのセットアップを確認してください
  - [pom.xml](./pom.xml#L31L35)
  - [application.conf](./src/main/resources/application.conf)
  - [http-main.conf](./src/main/resources/application.conf)
  - [backend-main.conf](./src/main/resources/application.conf)
  - [readside-main.conf](./src/main/resources/application.conf)

### 発展的内容:

- トレーニング1で考えたよう多数のテーブルを作成した場合、シーケンス図を書いてアクターからコマンド側の永続化層、クエリ側の永続仮想へと続く処理を整理してください

## 説明

- [課題背景](./BACKGROUND.md)
- [課題手順の詳細](./INSTRUCTION.md)

## 参考文献・資料

- https://plantuml.com/