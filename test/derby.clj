(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  derby
  (:use [feeds.db :as db])
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging])
  (:import java.sql.BatchUpdateException))

 (defn connection-props [connect-string user password]
      {:classname "org.apache.derby.jdbc.EmbeddedDriver"
       :subprotocol "derby"
       :subname "feeder.derby" 
       :create true})

(defn- dev-feeder-props [user password]                       
     (connection-props "feeder" user password))
 
(def  feed (dev-feeder-props "" ""))

(defn- create-feeder "Create derby table to store atom entries" []
 (try (sql/create-table :atoms [:id :int "PRIMARY KEY" "GENERATED ALWAYS AS IDENTITY"]
                           [:feed "varchar(255)"]
                           [:atom :clob]
                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])
   (catch BatchUpdateException e (prn e))))



(defn-  create-derby-database [] 
  (sql/with-connection feed (sql/transaction (create-feeder )) ))

(def records [ 
               ["sso","feed1"]
               ["sso","feed2"]
               ["sso","feed3"]
               ["sso","feed4"]
               ["sso","feed5"]
               ["sso","feed6"]
               ["kunde","feed1"]
               ["kunde","feed2"]
               ["kunde","feed3"]
               ] )

(defn insert-record []
  (map #(do 
          (prn "insert")
          (db/insert-atom-entry (first %) (last %))) records))


(defn init-derby [] 
  (do (prn "before init-derby")
      (create-derby-database )
      (logging/info "init-derby")
      (insert-record)))



