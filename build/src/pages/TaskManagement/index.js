import React, { Component, useEffect, Fragment } from 'react'
import { connect } from 'react-redux'
import Page1 from './page1'
import Page2 from './page2'
import * as actionCreators from './store/actionCreators'

export const DataSourceMng = (props) => {

    useEffect(() => {
        return () => {
            props.resetState()
        }
    }, [])


    const renderContent = () => {
        const { showPage } = props
        if (showPage === 1) {
            return <Page1 {...props} />
        } else {
            return <Page2 {...props} />
        }
    }

    return (
        <Fragment>
            {renderContent()}
        </Fragment>
    )
}

const mapStateToProps = (state) => {
    return { ...state.TaskMngReducer }
}

const mapDispatchToProps = (dispatch) => {
    return {
        resetState() {
            const action = actionCreators.resetState()
            dispatch(action)
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(DataSourceMng)
