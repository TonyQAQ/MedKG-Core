import { SET_PERSON_INFO } from './actionTypes'

const initialState = {
    userName: '',
    islogin: true,
    isClick: false,
    roleId: ""
}

export default (state = initialState, { type, payload }) => {
    let newState = JSON.parse(JSON.stringify(state));
    switch (type) {

        case SET_PERSON_INFO: {
            const { userName, roleId } = payload.info
            newState.userName = userName
            newState.roleId = roleId
            break;
        }
        case 'loginIn': {
            const { islogin } = payload
            newState.islogin = islogin
            break;
        }

        case 'loginOut': {
            const { islogin, userName } = payload
            newState.islogin = islogin
            newState.userName = userName
            break;
        }

        case 'backLogin': {
            const { islogin } = payload
            newState.islogin = islogin
            break;
        }

        case 'isClickLoginOut': {
            const { isClick } = payload
            newState.isClick = isClick
            break;
        }
        // default:
        //     debugger
        //     newState = initialState

    }
    return newState
}
