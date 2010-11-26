(ns unit.feed 
  
(:use     feeds.routes
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


(describe "find an atom entry" 
          (it "Eksisterende entry findes med resource sso og id 10 - return 200" 
              (= 200 (:status (find-entry "sso" "10"))))
          (it "Ikke eksisterende entry findes med resource sso og id -10 - return 404" 
              (= 404 (:status (find-entry "sso" "-10"))))
          (it "Entry forsoeges fundet med tom id - return 400" 
              (= 400 (:status (find-entry "sso" nil))))
          (it "Entry forsoeges fundet med tom resource - return 400" 
              (= 400 (:status (find-entry nil "10"))) ))
