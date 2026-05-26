import { INIT_TASKEXECUTION } from './actionTypes'

export const initTaskExecution = (isInit) => {
    return {
        type: INIT_TASKEXECUTION,
        payload: {
            isInit
        }
    }
}
