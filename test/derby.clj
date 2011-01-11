(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  derby
  (:use [feeds.db :as db]
        config)
  (:require [clojure.contrib.sql :as sql]
            [feeds.atoms :as atoms]
            [clojure.contrib.lazy-xml :as lazy ]
            [clojure.contrib.logging :as logging])
  (:import java.sql.BatchUpdateException))


(defn  feed [] (conj {:create true} (feed-db)))

(defn- create-feeder "Create derby table to store atom entries" []
 (try (sql/create-table :atoms [:id "varchar(40)" "PRIMARY KEY"]
                           [:feed "varchar(255)"]
                           [:atom :clob]
                           [:created_at :date "NOT NULL" "DEFAULT CURRENT_DATE"])
   (catch BatchUpdateException e )))


;(. ( java.util.UUID/randomUUID ) toString)    

(defn get-all [] 
 (sql/with-connection (feed) (sql/with-query-results rs ["select id, created_at from atoms"] 
    (vec rs))))


(defn-  create-derby-database [] 
  (sql/with-connection 
    (feed) 
    (sql/transaction (create-feeder ))))


(defn atom-entry  []
      `{:tag :entry, :attrs {}, :content 
        ({:tag :id, :attrs {}, :content ( ~(. ( java.util.UUID/randomUUID ) toString) )} 
         {:tag :title, :attrs 
           {:type "text"}, :content ("Test entry")} 
         {:tag :updated, :attrs {}, :content ("2009-07-01T11:58:00Z")})})


(def records 
  '(["sso"]))

(defn insert-records []
  (doall 
    (map #(atoms/add-xml-entry  
             (first %) 
             (with-out-str 
                (lazy/emit (atom-entry) :xml-declaration false :indent 2))) records)))


(defn init-derby [] 
  (do (create-derby-database )
      (insert-records )))



