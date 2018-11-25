import {
    ADD_MESSAGE,
    SEND_MESSAGE,
    CREATE_MESSAGE,
    LOG_IN,
    LOG_OUT, ADD_CLIENT, REMOVE_CLIENT, ADD_CLIENTS
} from "../Constants/actionTypes";
import uuid = require("uuid");
import Client from "../Models/client";
import User from "../Models/user";
import Message from "../Models/message";


export const messages = (state : Array<Message> = [], action : any) => {
    switch (action.type) {
        case ADD_MESSAGE:
            return [
                ...state,
                message({} as Message, action)
            ];
        default:
            return state;
    }
};

export const message = (state : Message = {} as Message, action : any) => {
    switch (action.type) {
        case ADD_MESSAGE:
            return {
                ...state,
                username : action.username,
                text : action.text,
                id : action.id,
                timestamp : action.timestamp
            } as Message;
        default:
            return state;
    }
};

export const user = (state : User = {} as User, action : any) => {
    switch (action.type) {
        case LOG_IN:
            return {
                isAuthorized : true,
                username : action.username
            } as User;
        case LOG_OUT:
            return {
                isAuthorized : false,
                username : ""
            } as User;
        default:
            return state;
    }
};

export const clients = (state : Array<Client> = [], action : any) => {
    switch (action.type) {
        case ADD_CLIENT:
            return [
                ...state,
                client({} as Client, action)
            ] as Client[];
        case ADD_CLIENTS:
            return [
                ...state,
                ...action.clients
            ] as Client[];
        case REMOVE_CLIENT:
            return state.filter((c) => c.id !== action.id);

        default:
            return state;
    }
};

export const client = (state : Client = {} as Client, action : any) => {
    switch (action.type) {
        case ADD_CLIENT:
            return {
                ...state,
                username : action.username,
                id : action.id,
                timestamp : action.timestamp
            } as Client;
        default:
            return state;
    }
};