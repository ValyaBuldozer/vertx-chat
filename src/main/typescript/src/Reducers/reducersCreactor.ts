import {
    CREATE_MESSAGE,
    ADD_MESSAGE,
    SEND_MESSAGE,
    LOG_IN,
    LOG_OUT, ADD_CLIENT
} from "../Constants/actionTypes";
import { v4 as uuid } from "uuid";

export const addMessage = ({username, text}) => ({
    type : ADD_MESSAGE,
    id : uuid(),
    username : username,
    text : text,
    timestamp : new Date().toString()
});

export const logIn = (username : string) => ({
    type : LOG_IN,
    username : username
});

export const addClient = (username : string) => ({
    type : ADD_CLIENT,
    username : username,
    id : uuid(),
    timestamp : new Date().toString()
})

