import myAxios from "../plugins/myAxios";
import {getCurrentUserState, setCurrentUserState} from "../states/user";
import {UserType} from "../models/user";

export const getCurrentUser = async () => {
    const currentUser = getCurrentUserState();
    if (currentUser) {
        return currentUser;
    }
    const res = await myAxios.get('/user/current');
    if (res.code === 0) {
        res.data = res.data as UserType;
        setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}

