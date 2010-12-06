(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  derby
  (:use [feeds.db :as db]
        feeds.config)
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging])
  (:import java.sql.BatchUpdateException))


(defn  feed [] (conj {:create true} (feed-db)))

(defn- create-feeder "Create derby table to store atom entries" []
 (try (sql/create-table :atoms [:id :int "PRIMARY KEY" "GENERATED ALWAYS AS IDENTITY"]
                           [:feed "varchar(255)"]
                           [:atom :clob]
                           [:created_at :date "NOT NULL" "DEFAULT CURRENT_DATE"])
   (catch BatchUpdateException e )))


(defn get-all [] 
 (sql/with-connection (feed) (sql/with-query-results rs ["select id, created_at from atoms"] 
    (vec rs))))

(defn-  create-derby-database [] 
  (sql/with-connection (feed) (sql/transaction (create-feeder )) ))

(def records '( 
               ["sso","feed1"]
               ["sso","feed2"]
               ["sso","feed3"]
               ["sso","feed4"]
               ["sso","feed5"]
               ["sso","feed6"]
               ["kunde","feed1"]
               ["kunde","feed2"]
               ["kunde","feed3"]
               ) )

(defn insert-records []
  (doall 
    (map #(db/insert-atom-entry (first %) (last %)) records)))


(defn init-derby [] 
  (do (create-derby-database )
      (insert-records )))



