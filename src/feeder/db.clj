(ns ^{:doc "Database layer for atom feeder" :author "Thomas Engelschmidt"}
  feeder.db
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))

 (defn- connection-props [connect-string user password]
      {:classname "org.apache.derby.jdbc.EmbeddedDriver"
       :subprotocol "derby"
       :subname "feeder.derby" 
       :create true})

(defn- dev-feeder-props [user password]                       
     (connection-props "feeder" user password))
 
(def  feeder (dev-feeder-props "" ""))

(defn- create-feeder "Create derby table to store atom entries" []
  (sql/create-table :atoms [:id :int "PRIMARY KEY" "GENERATED ALWAYS AS IDENTITY"]
                           [:resource "varchar(255)"]
                           [:atom :clob]
                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))


(defn  create-derby-database [] 
  (sql/with-connection feeder(sql/transaction (create-feeder )) ))


