import { INIT_TASKEXECUTION } from './actionTypes'

const initialState = {
    isInit: false
}

export default (state = initialState, { type, payload }) => {
    let newState = JSON.parse(JSON.stringify(state));
    switch (type) {
        case INIT_TASKEXECUTION: {
            debugger
            const { isInit } = payload
            newState.isInit = isInit
            break;
        }
    }
    return newState
}
