import {
    CREATE_MESSAGE,
    ADD_MESSAGE,
    SEND_MESSAGE,
    LOG_IN,
    LOG_OUT
} from "../Constants/actionTypes";
import { v4 as uuid } from "uuid";

export const addMessage = ({username, text}) => ({
    type : ADD_MESSAGE,
    id : uuid(),
    username : username,
    text : text,
    time : new Date().toString()
});

export const logIn = (username : string) => ({
    type : LOG_IN,
    username : username
});

