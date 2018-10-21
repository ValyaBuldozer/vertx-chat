import {combineReducers, createStore, applyMiddleware} from "redux";
import { devToolsEnhancer } from "redux-devtools-extension";
import { user, messages, clients } from"./Reducers/chatReducers";
import Client from "./Models/client";
import User from "./Models/user";
import Message from "./Models/message";



const initialStore : IState = {
    messages : [ ],
    user : {
        username : "",
        isAuthorized : false
    },
    clients : [ ]
};

export interface IState {
    messages: Array<Message>,
    user: User,
    clients: Array<Client>
}

export const store = createStore(
    combineReducers({messages, user, clients}),
    initialStore,
    devToolsEnhancer({name: "redux"})
);

