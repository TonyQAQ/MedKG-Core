import React, { Component, Fragment } from 'react';
import LayoutHeader from '@/components/Header';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Radio, Pagination } from 'antd';
import { MyButton, InputWrap } from '@/components/commonWrap';
import { Annotator, Action } from 'poplar-annotation';
import * as api from './services';
import $ from 'jquery';
import ColorPicker from 'rc-color-picker';
import debounce from 'lodash/debounce';
import { MenuFoldOutlined, FormOutlined, MinusCircleOutlined } from '@ant-design/icons';
import MyModal from '@/components/Modal'
import 'rc-color-picker/assets/index.css';
import '../index.less';

const { Option } = Select;

export default class Index extends Component {
    constructor(props) {
        super(props)

        this.state = {
            currentSelectIndex: null,
            currentSelectFieldIndex: null,
            currentSelectLabelIndex: null,
            liList: [],
            fieldList: [],
            labelList: [],
            svgList: [],
            typeList: [],
            spinning: true,
            spinning2: true,
            svgSpinning: false,
            taskId: '',
            visible: false,
            visible2: false,
            visible3: false,
            tableSelVal: null,
            svgAllInfo: {},
            currentSvg: null,
            publishState: '02',
            labelCategories: [],
            radioList: [],
            connectionCategories: [],
            radioConnectionList: [],
            radioValue: null,
            radioInputVal: null,
            color: '#1890FF',
            startIndex: null,
            endIndex: null,
            first: null,
            second: null,
            fromLabel: null,
            endLabel: null,
            liLevel: 'level1',
            allSaveInfo: [],
            pageCurrent: 1,
            pageSize: 10,
            pageCount: 0,
            tableWord: null,
            labelWord: null,
            typeSelVal: null,
            labelInputVal: null,
            tableOrigin: null,
            attrMappingId: null,
            attrOrigin: null,
            focusIndex: null,
            rowIndex: null,
            seletText: null,
            myModalVisible: false,
            modalTitle: '',
            modalContent: '',
            publishState: '',
            modalType: ''
        }
        this.searchInput = debounce(this.searchInput, 500)
        this.debounceSearchInput2 = debounce(this.debounceSearchInput2, 800)
    }
    componentDidMount = () => {
        const { taskId } = this.props.match.params
        this.setState({
            taskId
        }, () => {
            this.iniLeftTable()
            this.iniRightLabelList()
            this.iniRightSelList()
        })
    }


    // 初始化右侧目录列表
    iniRightLabelList = () => {
        const { taskId, labelWord, typeSelVal } = this.state
        api.iniRightLabelList({
            taskId,
            keyWord: labelWord,
            typeCode: typeSelVal
        }).then(res => {
            this.setState({
                labelList: res.data,
                spinning2: false
            })
        }).catch(err => {
            this.setState({
                spinning2: false
            })
        })
    }

    // 初始化右侧下拉框列表
    iniRightSelList = () => {
        api.iniRightSelList().then(res => {
            this.setState({
                typeList: res.data,
                typeSelVal: res.data[0].code
            })
        }).catch(err => { })
    }

    // 初始化中间SVG列表
    initLabelList = () => {
        return new Promise(resolve => {
            this.setState({
                svgSpinning: true
            })
            const { attrMappingId, tableOrigin, attrOrigin, taskId, pageCurrent, pageSize } = this.state
            api.getLabelList({
                attrMappingId,
                pageCount: pageCurrent,
                pageSize,
                taskId,
                tableName: tableOrigin,
                columnName: attrOrigin
            }).then(res => {
                let { allSaveInfo } = this.state
                res.data.map((item, index) => {
                    allSaveInfo.push({
                        attrMappingId: item.attrMappingId,
                        rowIndex: index + 1,
                        annotation: item.annotation
                    })
                })
                const temp1 = JSON.parse(JSON.stringify(res.labelCategories))
                const temp2 = JSON.parse(JSON.stringify(res.connectionCategories))
                this.setState({
                    allSaveInfo,
                    svgList: res.data,
                    labelCategories: temp1,
                    radioList: temp1,
                    connectionCategories: temp2,
                    radioConnectionList: temp2,
                    pageCount: res.totalCount,
                    svgSpinning: false
                }, resolve)
            }).catch(err => {
                this.setState({
                    svgSpinning: false
                })
            })
        })
    }

    // 初始化左侧一级目录
    iniLeftTable = () => {
        const { taskId, tableWord, tableSelVal } = this.state
        api.iniLeftTable({
            taskId,
            keyWord: tableWord,
            isSelect: tableSelVal
        }).then(res => {
            this.setState({
                liList: res.data,
                spinning: false
            })
        }).catch(err => {
            this.setState({
                spinning: false
            })
        })
    }

    // 初始化左侧二级目录
    iniLeftField = () => {
        const { taskId, tableWord, tableSelVal, tableId } = this.state
        api.iniLeftField({
            taskId,
            tableId,
            keyWord: tableWord,
            isSelect: tableSelVal
        }).then(res => {
            this.setState({
                fieldList: res.data
            })
        }).catch(err => { })
    }

    // 左侧目录type选择事件
    selChange(type, value) {
        if (type === 'leftSel') {
            this.setState({
                tableSelVal: value,
                currentSelectIndex: null,
                currentSelectFieldIndex: null
            }, () => {
                const { liLevel } = this.state
                if (liLevel === 'level1') {
                    this.iniLeftTable()
                } else if (liLevel === 'level2') {
                    this.iniLeftField()
                }
            })
        } else if (type === 'rightSel' || type === 'typeSel') {
            this.setState({
                typeSelVal: value
            }, () => {
                if (type === 'rightSel') {
                    this.iniRightLabelList()
                }
            })
        }
    }

    // 目录点击事件
    liClick(item, index, type) {
        if (type === 'level1') {
            this.setState({
                liLevel: 'level2',
                tableId: item.tableId,
                currentSelectIndex: index,
                tableOrigin: item.tableOrigin,
                tableSelVal: null,
                tableWord: null
            }, () => {
                this.iniLeftField()
            })
        } else if (type === 'level2') {
            this.setState({
                liLevel: 'level1',
                tableSelVal: null,
                currentSelectFieldIndex: null,
                tableWord: null
            }, () => {

            })
        } else if (type === 'field') {
            this.clearSvg()
            this.setState({
                currentSelectFieldIndex: index,
                attrMappingId: item.attrMappingId,
                attrOrigin: item.attrOrigin,
                svgList: [],
                pageCurrent: 1,
                pageCount: 0,
                pageSize: 10
            }, () => {
                this.initLabelList()
            })
        }
    }

    // SVG弹窗输入框事件
    searchInput = (type, value) => {
        if (type === 'search1') {
            let tempLabelArry
            if (value) {
                tempLabelArry = this.state.labelCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(this.state.labelCategories))
            }
            this.setState({
                radioList: tempLabelArry,
                radioInputVal: value,
                radioValue: null
            })
        } else if (type === 'search2') {
            let tempLabelArry
            if (value) {
                tempLabelArry = this.state.connectionCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(this.state.connectionCategories))
            }
            this.setState({
                radioConnectionList: tempLabelArry,
                radioInputVal: value,
                radioValue: null
            })
        }
    }

    // SVG弹窗输入框防抖
    debounceSearchInput(type, { target: { value } }) {
        this.searchInput(type, value)
    }

    // 左右侧目录输入框防抖
    debounceSearchInput2(type) {
        if (type === 'tableName') {
            const { liLevel } = this.state
            if (liLevel === 'level1') {
                this.iniLeftTable()
            } else if (liLevel === 'level2') {
                this.iniLeftField()
            }
        } else {
            this.iniRightLabelList()
        }
    }

    // 左右侧输入框事件
    searchInput2(type, { target: { value } }) {
        if (type === 'tableName') {
            this.setState({
                tableWord: value
            }, () => {
                this.debounceSearchInput2('tableName')
            })
        } else {
            this.setState({
                labelWord: value
            }, () => {
                this.debounceSearchInput2('tableName2')
            })
        }
    }

    // 按钮事件
    btnClick(type) {
        const { taskId, svgAllInfo, allSaveInfo } = this.state
        if (type === 'pre') {
            api.preClick({
                taskId
            }).then(res => {

            }).catch(err => { })
        } else if (type === 'save') {
            message.success('保存成功！')
        } else if (type === 'submit') {
            const { taskId } = this.state
            api.publishConfirmInfo({
                taskId
            }).then(res => {
                const { publishComfirm, publishState } = res.data
                this.setState({
                    modalContent: publishComfirm,
                    publishState,
                    myModalVisible: true,
                    modalTitle: ('发布')
                })
            })
        } else if (type === 'cancel') {
            window.close()
        }
    }

    // 新增初始化SVG
    initSvg = (svgAllInfo, currentSvg, startIndex, endIndex, type) => {
        for (const key in svgAllInfo) {
            let tempJson = svgAllInfo[key].store.json
            tempJson.labelCategories = this.state.labelCategories
            tempJson.connectionCategories = this.state.connectionCategories
            svgAllInfo[key].remove()
            svgAllInfo[key] = null
            svgAllInfo[key] = new Annotator(tempJson, document.getElementById('svgContainer' + key), {
                labelWidthCalcMethod: 'label'
            })
            svgAllInfo[key].on('textSelected', (startIndex, endIndex) => {
                this.setState({
                    visible: true,
                    currentSvg: key,
                    startIndex,
                    endIndex
                })
            });
            svgAllInfo[key].on('labelClicked', (id, event) => {
                // 输出用户点击的label的ID
            });
            svgAllInfo[key].on('twoLabelsClicked', (first, second) => {
                this.setState({
                    visible2: true,
                    currentSvg: key
                }, () => {
                    const { currentSvg } = this.state
                    let fromLabel = ''
                    let endLabel = ''
                    if (svgAllInfo[currentSvg]) {
                        svgAllInfo[currentSvg].store.json.labels.map(item => {
                            if (first === second) {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                    endLabel = fromLabel
                                }
                            } else {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                } else if (item.id === second) {
                                    endLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                }
                            }
                        })
                    }
                    this.setState({
                        first,
                        second,
                        fromLabel,
                        endLabel
                    })
                })
            });
            svgAllInfo[key].on('connectionRightClicked', (id, event) => {
                // 输出用户点击的Connection的ID, 点击鼠标的event
                this.setState({
                    currentSvg
                }, () => {
                    const { currentSvg } = this.state
                    svgAllInfo[currentSvg].applyAction(Action.Connection.Delete(id))
                    for (const key in svgAllInfo) {
                        if (svgAllInfo.hasOwnProperty(key)) {
                            console.log(svgAllInfo[key].store)

                        }
                    }
                })
            });
            svgAllInfo[key].on('labelRightClicked', (id, x, y) => {
                // 输出用户点击的label的ID, 被点击时鼠标的 X,Y 值
                this.setState({
                    currentSvg
                }, () => {
                    const { currentSvg } = this.state
                    svgAllInfo[currentSvg].applyAction(Action.Label.Delete(id))
                    for (const key in svgAllInfo) {
                        if (svgAllInfo.hasOwnProperty(key)) {
                            console.log(svgAllInfo[key].store)

                        }
                    }
                })
            });
            if (type === 'label') {
                if (key === currentSvg.toString()) {
                    svgAllInfo[key].applyAction(Action.Label.Create(this.state.labelCategories.length - 1, startIndex, endIndex))
                }
            } else {
                if (key === currentSvg.toString()) {
                    svgAllInfo[key].applyAction(Action.Connection.Create(this.state.connectionCategories.length - 1, startIndex, endIndex))
                }
            }
        }
    }

    // 渲染SVG
    renderSvg = (item, index) => {
        const len = $("#svgAllContainer").children("#svgContainer" + index).length
        if (len === 0) {
            $('#svgAllContainer').append("<div class='svgContainer' id='svgContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgContainer' + index)
        let { svgAllInfo } = this.state
        const tempArry = JSON.parse(JSON.stringify(item.annotation.labelCategories))
        const tempArry2 = JSON.parse(JSON.stringify(item.annotation.connectionCategories))
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgContainer' + index).children().length === 0) {
            let annotator = new Annotator({
                content: item.annotation.content,
                labelCategories: tempArry,
                labels: item.annotation.labels,
                connectionCategories: tempArry2,
                connections: item.annotation.connections,
            }, document.getElementById('svgContainer' + index), {
                labelWidthCalcMethod: 'max'
            })
            annotator.on('textSelected', (startIndex, endIndex) => {
                let flag = false
                const labels = svgAllInfo[index].store.json.labels
                const len = labels.length
                for (let index2 = 0; index2 < len; index2++) {
                    if (labels[index2].startIndex === startIndex && labels[index2].endIndex === endIndex) {
                        message.error('请勿重复添加实体！')
                        flag = true
                        break;
                    }

                }
                // 输出用户选取的那些字
                // let userChoosedCategoryId = getUserChoosedCategoryId();
                // annotator.applyAction(Action.Label.Create(1, startIndex, endIndex))
                if (!flag) {
                    let label = svgAllInfo[index].store.json.content.substring(startIndex, endIndex)
                    this.setState({
                        visible: true,
                        currentSvg: index,
                        rowIndex: item.rowIndex,
                        startIndex,
                        endIndex,
                        seletText: label,
                        modalType: 'textSelected'
                    })
                }
            });
            annotator.on('labelClicked', (id, event) => {
                // 输出用户点击的label的ID
            });
            annotator.on('twoLabelsClicked', (first, second) => {
                let flag = false
                const connections = svgAllInfo[index].store.json.connections
                const len = connections.length
                for (let index2 = 0; index2 < len; index2++) {
                    if (connections[index2].fromId === first && connections[index2].toId === second) {
                        message.error('请勿重复添加相同关系！')
                        flag = true
                        break;
                    }

                }
                if (!flag) {
                    this.setState({
                        visible2: true,
                        currentSvg: index,
                        rowIndex: item.rowIndex,
                        startIndex: first,
                        endIndex: second,
                        modalType: 'twoLabelsClicked'
                    }, () => {
                        const { currentSvg } = this.state
                        let fromLabel = ''
                        let endLabel = ''
                        let startIndex = ''
                        let endIndex = ''
                        if (svgAllInfo[currentSvg]) {
                            svgAllInfo[currentSvg].store.json.labels.map(item => {
                                if (first === second) {
                                    if (item.id === first) {
                                        fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                        endLabel = fromLabel
                                        startIndex = item.startIndex + "--AA--" + item.endIndex
                                        endIndex = startIndex
                                    }
                                } else {
                                    if (item.id === first) {
                                        fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                        startIndex = item.startIndex + "--AA--" + item.endIndex
                                    } else if (item.id === second) {
                                        endLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
                                        endIndex = item.startIndex + "--AA--" + item.endIndex
                                    }
                                }

                            })
                        }
                        this.setState({
                            first,
                            second,
                            fromLabel,
                            endLabel,
                            startIndex,
                            endIndex
                        })
                    })
                }
            })
            annotator.on('connectionRightClicked', (id, event) => {
                const _this = this
                // 输出用户点击的Connection的ID, 点击鼠标的event
                this.setState({
                    currentSvg: index,
                    rowIndex: item.rowIndex,
                }, () => {
                    Modal.confirm({
                        title: "确认删除吗？",
                        onOk() {
                            const { currentSvg, attrMappingId, rowIndex, taskId } = _this.state
                            let params = {}
                            svgAllInfo[currentSvg].store.json.connections.map(item => {
                                if (item.id === id) {
                                    const labels = svgAllInfo[currentSvg].store.json.labels
                                    const len = labels.length
                                    let startIndex, endIndex
                                    for (let idx = 0; idx < len; idx++) {
                                        if (startIndex && endIndex) {
                                            break;
                                        } else if (labels[idx].id === item.fromId) {
                                            startIndex = labels[idx].startIndex + "--AA--" + labels[idx].endIndex
                                        } else if (labels[idx].id === item.toId) {
                                            endIndex = labels[idx].startIndex + "--AA--" + labels[idx].endIndex
                                        }
                                    }
                                    params['attrMappingId'] = attrMappingId
                                    params['rowIndex'] = rowIndex
                                    params['labelId'] = item.categoryId
                                    params['fromId'] = startIndex
                                    params['toId'] = endIndex
                                }
                            })
                            api.delSvgInfo([params]).then(res => {
                                svgAllInfo[currentSvg].applyAction(Action.Connection.Delete(id))
                                api.saveSvgInfo({
                                    attrMappingId,
                                    rowIndex,
                                    annotation: svgAllInfo[currentSvg].store.json
                                }, taskId).then(res => {
                                    _this.iniRightLabelList()
                                    _this.iniLeftTable()
                                    _this.iniLeftField()
                                }).catch(err => { })
                            }).catch(err => { })
                        },
                        cancelButtonProps: {
                            className: 'mds-btn-warm',
                            style: {
                                marginLeft: '8px'
                            }
                        }
                    })
                })

            });
            annotator.on('labelRightClicked', (id, e) => {
                const _this = this
                // 输出用户点击的label的ID, 被点击时鼠标的 X,Y 值
                this.setState({
                    currentSvg: index,
                    rowIndex: item.rowIndex,
                }, () => {
                    Modal.confirm({
                        title: "确认删除吗？",
                        onOk() {
                            const { currentSvg, attrMappingId, rowIndex, taskId } = _this.state
                            let params = []
                            let flag = false
                            const obj1 = svgAllInfo[currentSvg].store.json.labels
                            const len1 = obj1.length
                            svgAllInfo[currentSvg].store.json.connections.map(item => {
                                if (item.fromId === id || item.toId === id) {
                                    flag = true
                                }
                            })
                            if (flag) {
                                message.error('当前实体含有关系，请先删除相关关系！')
                            } else {
                                for (let index = 0; index < len1; index++) {
                                    if (obj1[index].id === id) {
                                        params.push({
                                            attrMappingId,
                                            rowIndex,
                                            labelId: obj1[index].categoryId,
                                            fromId: obj1[index].startIndex,
                                            toId: obj1[index].endIndex
                                        })
                                        break;
                                    }
                                }
                                api.delSvgInfo(params).then(res => {
                                    svgAllInfo[currentSvg].applyAction(Action.Label.Delete(id))
                                    api.saveSvgInfo({
                                        attrMappingId,
                                        rowIndex,
                                        annotation: svgAllInfo[currentSvg].store.json
                                    }, taskId).then(res => {
                                        _this.iniRightLabelList()
                                        _this.iniLeftTable()
                                        _this.iniLeftField()
                                    }).catch(err => { })
                                }).catch(err => { })
                            }
                        },
                        cancelButtonProps: {
                            className: 'mds-btn-warm',
                            style: {
                                marginLeft: '8px'
                            }
                        }
                    })
                })
            });
            svgAllInfo[index] = annotator
            setTimeout(() => {
                this.setState({
                    svgAllInfo
                })
            }, 0);
        }
    }


    // 新增按钮事件
    newAddLabel = () => {
        const { attrMappingId, attrOrigin } = this.state
        if (attrMappingId && attrOrigin) {
            this.setState({
                visible3: true
            })
        } else {
            message.error('请选在左侧选择表和列！')
        }
    }

    // 新增标签或者关系弹窗输入框事件
    inputChange = (e) => {
        const { target: { value } } = e
        this.setState({
            labelInputVal: value
        })
    }

    saveMakeLabel = (labelId, type) => {
        const { radioValue, svgAllInfo, currentSvg, startIndex, endIndex, rowIndex, taskId, attrMappingId, first, second } = this.state
        api.saveMakeLabel({
            attrMappingId,
            rowIndex,
            labelId: labelId ? labelId : radioValue,
            fromId: startIndex,
            toId: endIndex,
            markFileName: '',
            tag: ''
        }).then(res => {
            if (type === 'label') {
                svgAllInfo[currentSvg].applyAction(Action.Label.Create(labelId ? labelId : radioValue, startIndex, endIndex))
            } else {
                svgAllInfo[currentSvg].applyAction(Action.Connection.Create(labelId ? labelId : radioValue, first, second))
            }

            this.setState({
                visible: false,
                visible2: false,
                radioValue: null,
                radioInputVal: null,
                color: '#1890FF',
                radioList: this.state.labelCategories,
                radioConnectionList: this.state.connectionCategories,
                svgAllInfo
            }, () => {
                api.saveSvgInfo({
                    attrMappingId,
                    rowIndex,
                    annotation: svgAllInfo[currentSvg].store.json
                }, taskId).then(res => {
                    this.iniRightLabelList()
                    this.iniLeftTable()
                    this.iniLeftField()
                }).catch(err => { })
            })
        }).catch(err => { })
    }

    // 弹窗确认事件
    modalOk(type) {
        const { radioValue, radioInputVal, typeSelVal, color, radioList, taskId, radioConnectionList } = this.state
        if (type === '1') {
            if (radioValue) {
                this.saveMakeLabel('', 'label')
            } else if (!radioValue && radioInputVal && radioList.length === 0) {
                api.addOrUpdataLabel({
                    labelName: radioInputVal,
                    typeCode: '01',
                    taskId,
                    color,
                    borderColor: color
                }).then(res => {
                    this.setState({ visible: false })
                    this.clearSvg()
                    this.initLabelList()
                        .then(() => {
                            this.saveMakeLabel(res.labelId, 'label')
                        })
                }).catch(err => { })
            } else {
                message.error('请选择标签或者输入标签！')
            }
        } else if (type === '2') {
            // if (radioInputVal) {
            //     if (radioValue === '' || radioValue === undefined || radioValue === null) {
            //         this.state.connectionCategories.push({
            //             id: this.state.connectionCategories.length,
            //             text: radioInputVal
            //         })
            //         this.setState({
            //             connectionCategories: this.state.connectionCategories,
            //             radioConnectionList: JSON.parse(JSON.stringify(this.state.connectionCategories)),
            //             visible2: false,
            //             radioValue: null,
            //             radioInputVal: null,
            //         }, () => {
            //             this.initSvg(svgAllInfo, currentSvg, first, second, 'connection')
            //         })
            //     } else {

            //     }
            // } else
            if (radioValue) {
                this.saveMakeLabel('', 'connection')
            } else if (!radioValue && radioInputVal && radioConnectionList.length === 0) {
                api.addOrUpdataLabel({
                    labelName: radioInputVal,
                    typeCode: '02',
                    taskId,
                    color,
                    borderColor: color
                }).then(res => {
                    this.setState({ visible2: false })
                    this.clearSvg()
                    this.initLabelList()
                        .then(() => {
                            this.saveMakeLabel(res.labelId, 'connection')
                        })
                }).catch(err => { })
            } else {
                message.error('请选择关系或者输入关系！')
            }
        } else if (type === 'confirmNewAdd') {
            const { labelInputVal, typeSelVal, taskId, color, typeList } = this.state
            if (!labelInputVal) {
                message.error('不能为空！')
            } else {
                api.addOrUpdataLabel({
                    labelName: labelInputVal,
                    typeCode: typeSelVal,
                    taskId,
                    color,
                    borderColor: color
                }).then(res => {
                    this.setState({
                        visible3: false,
                        labelInputVal: null,
                        typeSelVal: typeList[0].code,
                        color: '#1890FF'
                    }, () => {
                        this.clearSvg()
                        this.iniRightLabelList()
                        this.initLabelList()
                    })
                }).catch(err => { })
            }
        }
    }

    // 弹窗取消事件
    modalCancel = () => {
        this.setState({
            visible: false,
            visible2: false,
            visible3: false,
            radioValue: null,
            startIndex: null,
            endIndex: null,
            fromLabel: null,
            endLabel: null,
            labelInputVal: null,
            radioList: this.state.labelCategories
        })
    }

    // 选择颜色
    colorChange = (value) => {
        this.setState({
            color: value.color
        })
    }


    // 单选按钮事件
    radioChange({ target: { value } }) {
        this.setState({
            radioValue: value
        })
    }


    // 清空SVG
    clearSvg = () => {
        return new Promise(resolve => {
            let { svgAllInfo } = this.state
            for (const key in svgAllInfo) {
                if (svgAllInfo[key]) {
                    svgAllInfo[key].remove()
                    svgAllInfo[key] = null
                    svgAllInfo = {}
                }
            }
            if ($('#svgAllContainer').children('.svgContainer').length > 0) {
                $('#svgAllContainer').empty()
            }
            this.setState({
                svgAllInfo,
                svgList: []
            }, resolve)
        })
    }

    // 右侧目录li鼠标移入移出事件
    liMouseEvent(type, index) {
        if (type === 'enter') {
            this.setState({
                focusIndex: index
            })
        } else if (type === 'leave') {
            this.setState({
                focusIndex: null
            })
        }
    }

    // 页码、页面数大小改变事件
    pageChange = (page, pageSize) => {
        this.clearSvg()
        this.setState({
            pageCurrent: page,
            pageSize,
            svgList: []
        }, () => {
            this.initLabelList()
        })
    }

    // 提交弹窗确认
    confirmClick = () => {
        const { taskId } = this.state
        api.confirmPublish({
            taskId
        }).then(res => {
            message.success(res.retmsg)
            window.opener.location.reload();
            this.setState({
                myModalVisible: false
            }, () => {
                setTimeout(() => {
                    window.close()
                }, 500);
            })
        }).catch(err => { })
    }

    render() {
        const {
            liList, spinning, currentSelectIndex, visible, visible2, radioList,
            radioValue, endLabel, fromLabel, radioConnectionList, liLevel, labelList,
            pageCurrent, pageSize, pageCount, fieldList, currentSelectFieldIndex,
            tableSelVal, tableWord, svgList, spinning2, typeList, visible3,
            currentSelectLabelIndex, focusIndex, seletText, svgSpinning, myModalVisible,
            modalTitle, modalContent, publishState, modalType, typeSelVal
        } = this.state
        return (
            <div className='mds-all-layout-wrap' >
                <LayoutHeader />
                <div className='mds-executePage-wrap'>
                    <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                        <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                            <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                                <div className='mds-executePage-conditionWrapL'>
                                    <Select value={tableSelVal} onSelect={this.selChange.bind(this, 'leftSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                        <Option value='all'>全部</Option>
                                        <Option value='yes'>已选择</Option>
                                        <Option value='no'>未选择</Option>
                                    </Select>
                                    <Input value={tableWord} placeholder='请输入表名' onChange={this.searchInput2.bind(this, 'tableName')} />
                                </div>
                                <ul className={'mds-executePage-ulWrap'} style={liLevel === 'level1' ? {
                                    transform: 'translateX(0%)'
                                } : { transform: 'translateX(-101%)' }}>
                                    {
                                        liList.length > 0
                                            ?
                                            liList.map((item, index) => {
                                                return (
                                                    <Tooltip
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        getPopupContainer={() => document.getElementById('tooltipWrap')}
                                                        overlayClassName='liToolTip'
                                                        placement='right'
                                                        title={
                                                            <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
                                                            </div>
                                                        }
                                                    >
                                                        <li
                                                            onClick={this.liClick.bind(this, item, index, 'level1')}
                                                            className={
                                                                currentSelectIndex === index
                                                                    ?
                                                                    'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                    :
                                                                    (
                                                                        item.isSelect === 'no'
                                                                            ?
                                                                            'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                            :
                                                                            'mds-executePage-liWrap mds-executePage-liSelect'
                                                                    )

                                                            }
                                                        >
                                                            <span className='mds-executePage-liName'>{item.tableName}</span>
                                                        </li>
                                                    </Tooltip>
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </ul>
                                <div className={'mds-executePage-ulWrap'} style={liLevel === 'level2' ? {
                                    transform: 'translateX(0%)'
                                } : { transform: 'translateX(-101%)' }}>
                                    <ul>
                                        {
                                            fieldList.length > 0
                                                ?
                                                fieldList.map((item, index) => {
                                                    return (
                                                        <li
                                                            key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                            onClick={this.liClick.bind(this, item, index, 'field')}
                                                            className={
                                                                currentSelectFieldIndex === index
                                                                    ?
                                                                    'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                    :
                                                                    (
                                                                        item.isSelect === 'no'
                                                                            ?
                                                                            'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                            :
                                                                            'mds-executePage-liWrap mds-executePage-liSelect'
                                                                    )

                                                            }
                                                        >
                                                            <span className='mds-executePage-liName'>{item.attrName}</span>
                                                        </li>
                                                    )
                                                })
                                                :
                                                <Empty style={{ marginTop: '35px' }} />
                                        }
                                    </ul>
                                    {React.createElement(MenuFoldOutlined, {
                                        className: 'trigger',
                                        style: {
                                            display: liLevel === 'level1' ? 'none' : 'block',
                                        },
                                        onClick: this.liClick.bind(this, '', '', 'level2'),
                                    })}
                                </div>

                            </div>
                        </Spin>
                        <div className='mds-executePage-middleWrap' >
                            <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                <div id='svgAllContainer'>
                                    {
                                        svgList.length > 0
                                            ?
                                            svgList.map((item, index) => {
                                                return (
                                                    this.renderSvg(item, index)
                                                )
                                            })
                                            :
                                            <Empty />
                                    }
                                </div>
                            </Spin>
                            <Pagination
                                className='mds-executePage-svgPageNation'
                                current={pageCurrent}
                                showQuickJumper
                                pageSize={pageSize}
                                onChange={this.pageChange}
                                total={pageCount}
                            />
                        </div>
                        <Spin spinning={spinning2} tip='加载中...' style={{ zIndex: '1' }}>
                            <div className='mds-executePage-rightWrap' id='tooltipWrap'>
                                <div className='mds-executePage-conditionWrapL'>
                                    <Select value={typeSelVal} onSelect={this.selChange.bind(this, 'rightSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                        {
                                            typeList.map(item => {
                                                return (
                                                    <Option value={item.code}>{item.value}</Option>
                                                )
                                            })
                                        }
                                    </Select>
                                    {/* <Input placeholder='请输入表名' onChange={this.searchInput2.bind(this, 'tableName2')} /> */}
                                    <Button type='primary' onClick={this.newAddLabel}>新增</Button>
                                </div>
                                <ul className='mds-executePage-ulWrap'>
                                    {
                                        labelList.length > 0
                                            ?
                                            labelList.map((item, index) => {
                                                return (
                                                    <li
                                                        onMouseEnter={this.liMouseEvent.bind(this, 'enter', index)}
                                                        onMouseLeave={this.liMouseEvent.bind(this, 'leave', index)}
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        onClick={this.liClick.bind(this, item, index)}
                                                        className={
                                                            currentSelectLabelIndex === index
                                                                ?
                                                                'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                :
                                                                (
                                                                    item.isSelect === 'no'
                                                                        ?
                                                                        'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                        :
                                                                        'mds-executePage-liWrap mds-executePage-liSelect'
                                                                )

                                                        }
                                                    >
                                                        <span className='mds-executePage-liName'>{item.labelName}</span>
                                                        {/* {
                                                            focusIndex === index
                                                                ?
                                                                <FormOutlined
                                                                    className='liIcon editIcon'
                                                                />
                                                                :
                                                                null
                                                        }
                                                        {
                                                            focusIndex === index
                                                                ?
                                                                <MinusCircleOutlined
                                                                    className='liIcon deleteIcon'
                                                                />
                                                                :
                                                                null
                                                        } */}
                                                    </li>
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </ul>
                            </div>
                        </Spin>
                    </div>
                    <div className='mds-executePage-btns' >
                        {/* <MyButton
                            label='上一步'
                            onClick={this.btnClick.bind(this, 'pre')}
                            style={{ marginRight: '40px' }}
                        /> */}
                        <MyButton
                            label='保存'
                            onClick={this.btnClick.bind(this, 'save')}
                            style={{ marginRight: '40px' }}
                        />
                        <MyButton
                            label='提交'
                            onClick={this.btnClick.bind(this, 'submit')}
                            style={{ marginRight: '40px' }}
                        />
                        <MyButton
                            label='取消'
                            onClick={this.btnClick.bind(this, 'cancel')}
                            classType='warm'
                        />
                    </div>
                </div>
                <Modal
                    title="定义标签"
                    visible={visible}
                    onCancel={this.modalCancel}
                    destroyOnClose
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={this.modalOk.bind(this, '1')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={this.modalCancel} />
                        </Fragment>
                    }
                    bodyStyle={{
                        height: '50vh'
                    }}
                >
                    <div className='mds-LabelDefinition-modalBody'>
                        <p>文本：{seletText}</p>
                        <div className='mds-LabelDefinition-modalConditionWrap'>
                            <Input.Group compact style={{ display: 'flex' }}>
                                <Input onChange={this.debounceSearchInput.bind(this, 'search1')} />
                                <ColorPicker
                                    animation="slide-up"
                                    defaultColor={'#1890FF'}
                                    onChange={this.colorChange}
                                />
                            </Input.Group>
                            {
                                radioList.length > 0
                                    ?
                                    <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={this.radioChange.bind(this)} value={radioValue}>
                                        {
                                            radioList.map(item => {
                                                if (item.id !== 'originId') {
                                                    return (
                                                        <Radio style={{ color: item.color }} value={item.id}>{item.text}</Radio>
                                                    )
                                                }
                                            })
                                        }
                                    </Radio.Group>
                                    :
                                    <div style={{ textAlign: 'center', marginTop: '15px' }}>
                                        <Empty />
                                    </div>

                            }
                        </div>
                    </div>
                </Modal>
                <Modal
                    title="定义标签"
                    visible={visible2}
                    onCancel={this.modalCancel}
                    destroyOnClose
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={this.modalOk.bind(this, '2')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={this.modalCancel} />
                        </Fragment>
                    }
                >
                    <div className='mds-LabelDefinition-modalBody'>
                        <p>from：{fromLabel}</p>
                        <p>to：{endLabel}</p>
                        <div className='mds-LabelDefinition-modalConditionWrap'>
                            <Input.Group compact style={{ display: 'flex' }}>
                                <Input onChange={this.debounceSearchInput.bind(this, 'search2')} />
                                {
                                    modalType === 'textSelected'
                                        ?
                                        <ColorPicker
                                            animation="slide-up"
                                            defaultColor={'#1890FF'}
                                            onChange={this.colorChange}
                                        />
                                        :
                                        null
                                }

                            </Input.Group>
                            {
                                radioConnectionList.length > 0
                                    ?
                                    <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={this.radioChange.bind(this)} value={radioValue}>
                                        {
                                            radioConnectionList.map(item => {
                                                return (
                                                    <Radio value={item.id}>{item.text}</Radio>
                                                )
                                            })
                                        }
                                    </Radio.Group>
                                    :
                                    <div style={{ textAlign: 'center', marginTop: '15px' }}>
                                        <Empty />
                                    </div>
                            }
                        </div>
                    </div>
                </Modal>
                <Modal
                    title="新增标签"
                    visible={visible3}
                    onCancel={this.modalCancel}
                    destroyOnClose
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={this.modalOk.bind(this, 'confirmNewAdd')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={this.modalCancel} />
                        </Fragment>
                    }
                >
                    <div className='mds-LabelDefinition-newAddModal'>
                        <Input.Group compact style={{ display: 'flex' }}>
                            <Select value={typeSelVal} onSelect={this.selChange.bind(this, 'typeSel')} placeholder='请选择' >
                                {
                                    typeList.map(item => {
                                        if (item.value !== '全部') {
                                            return (
                                                <Option value={item.code}>{item.value}</Option>
                                            )
                                        }
                                    })
                                }
                            </Select>
                            <Input onChange={this.inputChange} />
                            <ColorPicker
                                animation="slide-up"
                                defaultColor={'#1890FF'}
                                onChange={this.colorChange}
                            />
                        </Input.Group>
                    </div>
                </Modal>
                <MyModal
                    visible={myModalVisible}
                    // modalOk={modalOk}
                    modalCancel={() => {
                        this.setState({
                            myModalVisible: false
                        })
                    }}
                    title={modalTitle}
                    footer={
                        publishState === '01'
                            ?
                            <Button type={'primary'} onClick={() => {
                                this.setState({
                                    myModalVisible: false
                                })
                            }}>
                                {'确定'}
                            </Button>
                            :
                            <Fragment>
                                <Button type={'primary'} onClick={this.confirmClick}>确定</Button>
                                <MyButton label='取消' classType='warm' onClick={() => {
                                    this.setState({
                                        myModalVisible: false
                                    })
                                }} />
                            </Fragment>

                    }
                >
                    <Fragment>
                        <p dangerouslySetInnerHTML={{ __html: modalContent }}></p>
                    </Fragment>
                </MyModal>
            </div>
        )
    }
}

