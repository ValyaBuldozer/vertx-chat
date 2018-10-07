import {combineReducers, createStore} from "redux";

import { user, messages } from"./Reducers/chatReducers";

const initialStore = {
    messages : [],
    user : {
        username : "",
        isAuthorized : false
    }
}

export const store = createStore(
    combineReducers({messages, user}),
    initialStore
)