import {UserType} from "../models/user";
import {ref} from "vue";


const currentUser = ref<UserType | null>(null);


const setCurrentUserState = (user: UserType) => {
    currentUser.value = user;
}

const getCurrentUserState = () => {
    return currentUser.value;
}

export {
    setCurrentUserState,
    getCurrentUserState,
}