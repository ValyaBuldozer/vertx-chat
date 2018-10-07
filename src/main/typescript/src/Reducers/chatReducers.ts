import { ADD_MESSAGE,
    SEND_MESSAGE,
    CREATE_MESSAGE,
    LOG_IN,
    LOG_OUT
} from "../Constants/actionTypes";

export const messages = (state = [], action) => {
    switch (action.type) {
        case ADD_MESSAGE:
            return [
                ...state,
                message({}, action)
            ];
        default:
            return state;
    }
};

export const message = (state = {}, action) => {
    switch (action.type) {
        case ADD_MESSAGE:
            return {
                username : action.username,
                text : action.text,
                id : action.id,
                time : action.time
            };
        default:
            return state;
    }
};

export const user = (state = {}, action) => {
    switch (action.type) {
        case LOG_IN:
            return {
                isAuthorized : true,
                username : action.username
            }
        case LOG_OUT:
            return {
                isAuthorized : false,
                username : ""
            }
        default:
            return state;
    }
}