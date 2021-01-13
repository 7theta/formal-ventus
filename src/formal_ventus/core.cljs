(ns formal-ventus.core
  (:require [formal.core :as f]
            [formal.util.component :as c]
            [ventus.tailwind-ui :as tw]
            [ventus.util.classes :refer [join-classes]]
            [utilis.js :as j]
            [utilis.types.number :refer [string->double string->long]]
            [reagent.core :as r]))

;;; Forms

(defn error-typography
  [{:keys [error]}]
  (when error
    [tw/typography {:color :error}
     (str (or (when-let [error (-> error meta :human)]
                (cond
                  (and (sequential? error)
                       (= 1 (count error)))
                  (first error)

                  :else error))
              error))]))

(defn form
  [{:keys [schema] :as props}]
  [f/form (assoc props :namespace :ventus)])

(defn form-item
  []
  (r/create-class
   {:render (fn [this]
              (let [{:keys [namespace error label label-position input-container-classes]
                     :or {label-position :start}} (r/props this)
                    {:keys [input-id]} (r/state this)]
                [:div {:class-name "py-2"}
                 [:div {:class-name input-container-classes}
                  (when (and label (= label-position :start))
                    [:label {:for input-id} label])
                  (when-let [input (first (r/children this))]
                    (c/assoc-prop input :id input-id))
                  (when (and label (= label-position :end))
                    [:label {:for input-id} label])]
                 [error-typography {:error error}]]))
    :get-initial-state (fn [this]
                         {:input-id (str "html-input-" (gensym))})}))

;;; Inputs

(defn string-input
  [{:keys [on-change error] :as props}]
  [form-item props
   [tw/input (-> props
                 (select-keys [:default-value :placeholder])
                 (assoc :type :text
                        :error (boolean error)
                        :on-change #(on-change (j/get-in % [:currentTarget :value]))))]])

(defn integer-input
  [{:keys [on-change error] :as props}]
  [form-item props
   [tw/input (-> props
                 (select-keys [:default-value :placeholder])
                 (assoc :type :number
                        :error (boolean error)
                        :on-change #(on-change
                                     (string->long
                                      (j/get-in % [:currentTarget :value])))))]])

(defn boolean-input
  [{:keys [on-change default-value] :as props}]
  (let [this (r/current-component)
        {:keys [checked] :or {checked default-value}} (r/state this)]
    [form-item (assoc props
                      :label-position :end
                      :input-container-classes "flex justify-start items-center")
     [tw/switch {:checked (boolean checked)
                 :class-name :mr-1
                 :on-change #(let [checked (not checked)]
                               (on-change checked)
                               (r/set-state this {:checked checked}))}]]))

(defn map-input
  [{:keys [error inputs id]}]
  [:div
   (into [:div {:class-name (join-classes (when id :pl-6))}]
         (concat (->> inputs
                      (sort-by first)
                      (map second))
                 [[error-typography {:error error}]]))])

(defn sequential-input
  [{:keys [inputs on-change value error] :as props}]
  (into [:div]
        (concat inputs
                [[:button {:on-click #(on-change (conj value nil))} "Add"]
                 [error-typography {:error error}]])))

(f/reg-input :ventus/integer integer-input)
(f/reg-input :ventus/number integer-input)
(f/reg-input :ventus/string string-input)
(f/reg-input :ventus/boolean boolean-input)
(f/reg-input :ventus/map map-input)
(f/reg-input :ventus/sequential sequential-input)
