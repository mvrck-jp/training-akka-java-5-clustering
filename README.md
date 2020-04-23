JavaによるAkkaトレーニング第5回 

## Akkaクラスタリング

いよいよAkkaクラスタリング機能の紹介です。
アクター、イベントソーシングによるCQRS、そしてクラスタリングを用いることでAkkaの力を最大限活かすことが出来る、というのが私の考えです。
ありとあらゆるシステムでこれら全ての

このトレーニングではCQRS - Command Query Responsibility Separationと呼ばれる設計パターンの中を使ってイベント・ソーシングでは重視しなかったデータ読み込み側の処理を補完します。

以下に課題を用意したので、それをこなすことがこのトレーニングのゴールです。
課題の提出方法は後ほど紹介しますが、課題を通じて手を動かすとともに、トレーナーと対話することで学びを促進することが狙いです。

- [課題提出トレーニングのポリシー](./POLICIES.md)
- [次回のトレーニング: Akkaアクターを用いた非同期処理](https://github.com/mvrck-inc/training-akka-java-3-event-sourcing)

## この課題で身につく能力

- Akka HTTPと、別プロセスになっているアクターを用いたバックエンドとのAkkaクラスタリングによる接続
- CQRSとの統合
- Akka HTTPとAkkaバックエンドそれぞれを複数プロセス立ち上げスケールさせる
- Akkaクラスタリングアプリケーション全体のパフォーマンスを測定する

## 課題

MacBook前提。

## 課題

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
- akka-persistence-queryをセットアップしてください
- データベースのセットアップをしてください
  - `CREATE TABLE`を走らせてください(リンク)
- もうひとつのdef main()を作成してください
- def main() の中から注文とチケット在庫、2つのRead Journalをスタートしてprintlnください
- printlnの代わりにMySQLのテーブルに書き込んでください


http-mainの中身を確かめてください - clustersharding, shardregion
entitykeyを確かめてください
backend-main

### 発展的内容:

- トレーニング1で考えたよう多数のテーブルを作成した場合、シーケンス図を書いてアクターからコマンド側の永続化層、クエリ側の永続仮想へと続く処理を整理してください

## 説明

- [課題背景](./BACKGROUND.md)
- [課題提出方法](./SUBMIT.md)
- [課題手順の詳細](./DETAILES.md)

## 参考文献・資料

- https://plantuml.com/