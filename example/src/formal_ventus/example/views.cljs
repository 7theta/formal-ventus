(ns formal-ventus.example.views
  (:require [formal-ventus.core :as fv]
            [ventus-ui.button :as button]
            [inflections.core :as inflections]
            [sci.core]
            [tailwind.core :refer [tw]]
            [reagent.core :as r]))

(defn basic-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:first-name :string]
             [:last-name :string]
             [:country [:enum :canada :united-states]]
             [:enabled :boolean]
             [:about {:optional true
                      :placeholder "About"}
              :string]
             [:toggle :boolean]]
    :on-change on-change}])

(defn default-value-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:first-name :string]
             [:last-name :string]
             [:about {:optional true} :string]]
    :default-value {:first-name "Tom"
                    :last-name "Bombadil"}
    :on-change on-change}])

(defn nested-maps-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:profile {:title "Profile"
                        :subtitle "This information will be displayed publicly so be careful what you share."}
              [:map
               [:first-name :string]
               [:last-name :string]
               [:about {:optional true} :string]]]
             [:contact-information {:title "Personal Information"
                                    :subtitle "Use a permanent address where you can receive mail."}
              [:map
               [:phone-number :string]
               [:email :string]]]]
    :on-change on-change}])

(defn sequences-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:list-of-strings
              [:sequential :string]]]
    :on-change on-change}])

(defn custom-errors-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:username
              [:string {:min 10
                        :error/fn '(fn [{:keys [value]} _]
                                     "Must be at least 10 characters.")}]]]
    :on-change on-change}])

(defn custom-schema-types-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:age {:default-value 0
                    :input :number}
              [:fn {:error/fn '(fn [{:keys [value]} _]
                                 (str value " should be > 0"))}
               pos?]]]
    :on-change on-change}])

(defn custom-layouts-example
  [{:keys [on-change]}]
  [fv/form
   {:schema [:map
             [:profile
              [:map
               [:first-name :string]
               [:last-name :string]
               [:about {:optional true} :string]]]
             [:contact-information
              [:map
               [:phone-number :string]
               [:email :string]]]]
    :components {:ventus/map (fn [{:keys [index value error inputs id] :as props}]
                               [:div {:class-name (tw (concat
                                                       [:p-4]
                                                       (when (and index (pos? index))
                                                         [:mt-2])))}
                                (into
                                 [:div "Custom Map Component"]
                                 (concat
                                  (->> inputs
                                       (sort-by (comp :index :props second))
                                       (map (fn [[_ {:keys [render props]}]]
                                              [render props]))
                                       (doall))
                                  (when (and error (seq value))
                                    [[:div {:class-name (tw [:mt-2])}
                                      [fv/error-typography {:error error}]]])))])}
    :on-change on-change}])

(defn main-panel
  []
  (let [this (r/current-component)
        {:keys [example value] :or {example :basic}} (r/state this)
        examples [[:basic basic-example]
                  [:default-value default-value-example]
                  [:nested-maps nested-maps-example]
                  [:sequences sequences-example]
                  [:custom-errors custom-errors-example]
                  [:custom-schema-types custom-schema-types-example]
                  [:custom-layouts custom-layouts-example]]
        examples-map (into {} examples)]
    [:div {:class-name "w-full"}
     [:div {:class-name (tw [:flex :flex-row :items-start])}
      [:div {:class-name (tw [:max-w-md :w-full])}

       [fv/form
        {:on-change (fn [{:keys [example]}]
                      (r/set-state this {:example example}))
         :schema [:map
                  [:example {:default-value :basic}
                   (into [:enum] (map first examples))]]}]

       (when-let [c (get examples-map example)]
         [c {:key example
             :on-change #(r/set-state this {:value %})}])]
      (when value
        [:div {:class-name (tw [:max-w-sm :w-full :p-4])}
         [:h3 {:class-name (tw [:leading-6 :font-medium :text-gray-900 :text-lg :py-2])}
          "Example Value"]
         [:p {:class-name (tw [:block :text-sm :font-medium :text-gray-700 :whitespace-pre-line])}
          (-> value
              cljs.pprint/pprint
              with-out-str)]])]]))
