import {ADD_CLIENT, ADD_CLIENTS, ADD_MESSAGE, LOG_IN} from "../Constants/actionTypes";
import {v4 as uuid} from "uuid";

export const addMessage = ({username, text}) => ({
    type: ADD_MESSAGE,
    id: uuid(),
    username: username,
    text: text,
    timestamp: new Date().toString()
});

export const logIn = (username: string) => ({
    type: LOG_IN,
    username: username
});

export const addClient = (username: string) => ({
    type: ADD_CLIENT,
    username: username,
    id: uuid(),
    timestamp: new Date().toString()
});

export const addClients = (clients: string[]) => ({
    type: ADD_CLIENTS,
    clients: clients.map((client) => ({username: client, id: uuid(), timestamp: new Date().toString()}))
});

