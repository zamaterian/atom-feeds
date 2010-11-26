(ns unit.db 
    (:use     derby  
              lazytest.context.stub
              [lazytest.context :only (fn-context)]
              [lazytest.describe :only (describe it given do-it for-any with before)]
              [lazytest.expect :only (expect)]
              [lazytest.expect.thrown :only (throws?)]
              ))

(describe "Init derby base"
            (with [(before (init-derby))]
            (it "initialiser derby database"
              true)))

