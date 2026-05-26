import { SET_PERSON_INFO } from './actionTypes'


//保存登录人信息
export const setPersonInfo = (info) => {
    return {
        type: SET_PERSON_INFO,
        payload: {
            info
        }
    }
}
