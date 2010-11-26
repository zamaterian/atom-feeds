(ns unit.feed 
(:use     feeds.routes
          feeds.db
          lazytest.context.stub
          [lazytest.context :only (fn-context)]
          [lazytest.describe :only (describe it given do-it for-any with)]
          [lazytest.expect :only (expect)]
          [lazytest.expect.thrown :only (throws?)]
          [lazytest.random :as r]))


(defn find-feed [resource d m y]
    (app {:request-method :get :uri (str "/" resource "/feed/" d "/" m "/" y "/")}))
(defn find-entry [resource id]
    (app {:request-method :get :uri (str "/" resource "/notification/" id)}))


(def find-atom-entry-ok (stub #'find-atom-entry (constantly {"first" "Steen"})))
(def find-atom-entry-error (stub #'find-atom-entry (constantly nil)))

(describe "find an atom entry" 
         (with [find-atom-entry-ok] 
             (it "Eksisterende entry findes med resource sso og id 10 - return 200" 
               (= 200 (:status (find-entry "sso" "10")))))
          (with [find-atom-entry-error]
            (it "Ikke eksisterende entry findes med resource sso og id -10 - return 404" 
              (= 404 (:status (find-entry "sso" "-10"))))
            (it "Entry forsoeges fundet med tom id - return 400" 
              (= 404 (:status (find-entry "sso" nil))))
            (it "Entry forsoeges fundet med tom resource - return 400" 
              (= 404 (:status (find-entry "" "10"))) )))
