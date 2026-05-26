import React, { Component, Fragment, useEffect, useState } from 'react';
import LayoutHeader from '@/components/Header';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Tree, Pagination, Radio } from 'antd';
import { MyButton, InputWrap } from '@/components/commonWrap';
import { Annotator, Action } from 'poplar-annotation';
import { FormOutlined, MinusCircleOutlined } from '@ant-design/icons';
import * as api from './services';
import ColorPicker from 'rc-color-picker';
import MyModal from '@/components/Modal'
import $ from 'jquery'
import 'rc-color-picker/assets/index.css';
import './index.less';

const { Option } = Select

export default function Index(props) {
    const [taskId, setTaskId] = useState(null)
    const [spinning, setSpinning] = useState(true)
    const [spinning2, setSpinning2] = useState(false)
    const [svgSpinning, setSvgSpinning] = useState(false)
    const [labelAddVisible, setLabelAddVisible] = useState(false)
    const [keyWord, setKeyWord] = useState('')
    const [svgList, setSvgList] = useState([])
    const [labelTypeList, setLabelTypeList] = useState([])
    const [schemaLiList, setSchemaLiList] = useState([])
    const [currentSelectLabelIndex, setcurrentSelectLabelIndex] = useState(null)
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageCount, setPageCount] = useState(0)
    const [typeSelVal, setTypeSelVal] = useState(null)
    const [typeSelVal2, setTypeSelVal2] = useState(null)
    const [color, setColor] = useState('#1890FF')
    const [color2, setColor2] = useState('#1890FF')
    const [newAddLabelVal, setNewAddLabelVal] = useState(null)
    const [treeData, setTreeData] = useState([])
    const [treeDataTemp, setTreeDataTemp] = useState([])
    const [radioConnectionList, setRadioConnectionList] = useState([])
    const [radioList, setRadioList] = useState([])
    const [labelCategories, setLabelCategories] = useState([])
    const [connectionCategories, setConnectionCategories] = useState([])
    const [paths, setPaths] = useState('')
    const [isLeaf, setIsLeaf] = useState(true)
    const [svgAllInfo, setSvgAllInfo] = useState({})
    const [currentSvg, setCurrentSvg] = useState(null)
    const [startIndex, setStartIndex] = useState(null)
    const [endIndex, setEndIndex] = useState(null)
    const [seletText, setSeletText] = useState(null)
    const [visible, setVisible] = useState(false)
    const [visible2, setVisible2] = useState(false)
    const [first, setFirst] = useState(null)
    const [second, setSecond] = useState(null)
    const [fromLabel, setFromLabel] = useState(null)
    const [endLabel, setEndLabel] = useState(null)
    const [radioValue, setRadioValue] = useState(null)
    const [radioInputVal, setRadioInputVal] = useState(null)
    const [canGetSvg, setCanGetSvg] = useState(false)
    const [labelId, setLabelId] = useState(null)
    const [labelIds, setlabelIds] = useState([])
    const [connectionId, setConnectionId] = useState(null)
    const [connectionIds, setConnectionIds] = useState([])
    const [currentSvgObj, setCurrentSvgObj] = useState(null)
    const [focusIndex, setFocusIndex] = useState(null)
    const [modalLabelInput, setModalLabelInput] = useState(null)
    const [labelModalIsEdit, setLabelModalIsEdit] = useState(false)
    const [labelIndex, setLabelIndex] = useState(null)
    const [expandedKeys, setExpandedKeys] = useState([])
    const [autoExpandParent, setAutoExpandParent] = useState(true)
    const [treeDataList, setTreeDataList] = useState([])
    const [myModalVisible, setMyModalVisible] = useState(false)
    const [selectedKeys, setSelectedKeys] = useState([])

    useEffect(() => {
        if (Object.keys(svgAllInfo).length > 0 && labelId && (labelIds.indexOf(labelId) > -1)) {
            svgAllInfo[currentSvg].applyAction(Action.Label.Create(labelId, startIndex, endIndex))
            currentSvgObj.annotation = svgAllInfo[currentSvg].store.json
            setLabelId(null)
            api.saveSvgInfo([currentSvgObj], taskId, paths).then(res => {
                setVisible(false)
                setRadioValue(null)
                setRadioInputVal(null)
            }).catch(err => {
                setCanGetSvg(true)
            })
        } else if (Object.keys(svgAllInfo).length > 0 && connectionId && (connectionIds.indexOf(connectionId) > -1)) {
            svgAllInfo[currentSvg].applyAction(Action.Connection.Create(connectionId, first, second))
            currentSvgObj.annotation = svgAllInfo[currentSvg].store.json
            setConnectionId(null)
            api.saveSvgInfo([currentSvgObj], taskId, paths).then(res => {
                setVisible2(false)
                setRadioValue(null)
                setRadioInputVal(null)
            }).catch(err => {
                setCanGetSvg(true)
            })
        }
    })
    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
        getLabelTypeList()
    }, [])

    useEffect(() => {
        if (taskId) {
            getTreeData()
            getSchmaList()
        }
    }, [taskId])

    useEffect(() => {
        if (paths && isLeaf && pageCurrent) {
            getSvgList()
        }
    }, [paths, isLeaf, pageCurrent])

    useEffect(() => {
        if (canGetSvg) {
            getSvgList()
        }
    }, [canGetSvg])


    const getTreeData = () => {
        api.getTreeData({
            taskId
        }).then(res => {
            const { tree } = res
            const newTree = repalceTree(tree)
            let dataList = [], paths, openKeys = [], selectedKeys = []
            const generateList = data => {
                for (let i = 0; i < data.length; i++) {
                    const node = data[i];
                    const { key, title } = node;
                    dataList.push({ key, title });
                    if (node.children) {
                        generateList(node.children);
                    }
                }
            };
            const getFirstPaths = (data) => {
                for (let index = 0; index < data.length; index++) {
                    const item = data[index];
                    if (item.children && item.children.length > 0) {
                        openKeys.push(item.key)
                        return getFirstPaths(item.children)
                    } else {
                        paths = item.path
                        selectedKeys.push(item.key)
                        break;
                    }
                }
            }
            getFirstPaths(newTree)
            generateList(newTree)
            setPaths(paths)
            setSelectedKeys(selectedKeys)
            setTreeDataList(dataList)
            setExpandedKeys(openKeys)
            setTreeData(newTree)
            setTreeDataTemp(newTree)
            setSpinning(false)
        }).catch(err => {
            setSpinning(false)
        })
    }

    const repalceTree = (tree, node) => {
        return tree.map(item => {
            if (item.children && item.children.length > 0) {
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                repalceTree(item.children, item)
                return item
            } else {
                item.isLeaf = true
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                return item
            }
        })
    }

    // 获取标签分类列表
    const getLabelTypeList = () => {
        api.getLabelTypeList().then(res => {
            const { data } = res
            setLabelTypeList(data)
            setTypeSelVal(data.length > 0 ? data[0].code : null)
            setTypeSelVal2(data.length > 0 ? data[0].code : null)
        }).catch(err => { })
    }
    //获取schmaList
    const getSchmaList = () => {
        api.getSchmaList({
            taskId
        }).then(res => {
            const { connectionCategories, labelCategories } = res
            const temp1 = JSON.parse(JSON.stringify(res.labelCategories))
            const temp2 = JSON.parse(JSON.stringify(res.connectionCategories))
            let ids = []
            let ids2 = []
            temp1.map(item => {
                ids.push(item.id)
            })
            temp2.map(item => {
                ids2.push(item.id)
            })
            setlabelIds(ids)
            setConnectionIds(ids2)
            setLabelCategories(temp1)
            setSchemaLiList(typeSelVal === '01' || typeSelVal === null ? temp1 : temp2)
            setRadioList(temp1)
            setRadioConnectionList(temp2)
            setConnectionCategories(temp2)
        }).catch(err => { })
    }
    // 获取svg列表
    const getSvgList = () => {
        api.getSvgList({
            taskId,
            pageSize,
            pageCount: pageCurrent,
            paths
        }).then(res => {
            setSvgList(res.data)
            setPageCount(res.totalCount)
            setSvgSpinning(false)
            setCanGetSvg(false)
        }).catch(err => {

        })
    }
    // 翻页
    const pageChange = (page, pageSize) => {
        $('.svgMainContainer').remove()
        setSvgSpinning(true)
        setSvgList([])
        setSvgAllInfo({})
        setPageCurrent(page)
    }
    // 弹窗取消
    const modalCancel = () => {
        setLabelAddVisible(false)
        setVisible(false)
        setVisible2(false)
        setRadioValue(null)
        setRadioInputVal(null)
        setLabelModalIsEdit(false)
        setModalLabelInput(null)
        setNewAddLabelVal(null)
    }
    // 树节点选择
    const onSelect = (selectedKeys, e) => {
        if (e.node.path !== paths) {
            if (e.node.isLeaf) {
                $('.svgMainContainer').remove()
                setSvgSpinning(true)
                setSvgList([])
                setSvgAllInfo({})
                setPageCurrent(1)
            }
            setPaths(e.node.path)
            setIsLeaf(e.node.isLeaf)
            setSelectedKeys(selectedKeys)
        }
    }
    // 渲染SVG
    const renderSvg = (item, index) => {
        const len = $("#svgMainAllContainer").children("#svgMainContainer" + index).length
        if (len === 0) {
            $('#svgMainAllContainer').append("<div class='svgMainContainer' id='svgMainContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgMainContainer' + index)
        const tempArry = JSON.parse(JSON.stringify(item.annotation.labelCategories))
        const tempArry2 = JSON.parse(JSON.stringify(item.annotation.connectionCategories))
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgMainContainer' + index).children().length === 0) {
            let annotator = new Annotator({
                content: item.annotation.content,
                labelCategories: tempArry,
                labels: item.annotation.labels,
                connectionCategories: tempArry2,
                connections: item.annotation.connections,
            }, document.getElementById('svgMainContainer' + index), {
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
                    setCurrentSvgObj(item)
                    setVisible(true)
                    setCurrentSvg(index)
                    setStartIndex(startIndex)
                    setEndIndex(endIndex)
                    setSeletText(label)
                }
            });
            annotator.on('twoLabelsClicked', (first, second) => {
                let fromLabel = ''
                let endLabel = ''
                let startIndex = ''
                let endIndex = ''
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
                    if (svgAllInfo[index]) {
                        svgAllInfo[index].store.json.labels.map(item => {
                            if (first === second) {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    endLabel = fromLabel
                                    startIndex = item.startIndex + "--AA--" + item.endIndex
                                    endIndex = startIndex
                                }
                            } else {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    startIndex = item.startIndex + "--AA--" + item.endIndex
                                } else if (item.id === second) {
                                    endLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    endIndex = item.startIndex + "--AA--" + item.endIndex
                                }
                            }
                        })
                    }
                    setVisible2(true)
                    setCurrentSvg(index)
                    setCurrentSvgObj(item)
                    setStartIndex(startIndex)
                    setEndIndex(endIndex)
                    setFirst(first)
                    setSecond(second)
                    setFromLabel(fromLabel)
                    setEndLabel(endLabel)
                }
            })
            annotator.on('connectionRightClicked', (id, event) => {
                Modal.confirm({
                    title: "确认删除吗？",
                    onOk() {
                        annotator.applyAction(Action.Connection.Delete(id))
                        item.annotation = annotator.store.json
                        api.saveSvgInfo([item], taskId, paths).then(res => {
                            setRadioValue(null)
                            setRadioInputVal(null)
                        }).catch(err => {
                            canGetSvg(true)
                        })
                    },
                    cancelButtonProps: {
                        className: 'mds-btn-warm',
                        style: {
                            marginLeft: '8px'
                        }
                    }
                })
            });
            annotator.on('labelRightClicked', (id, e) => {
                Modal.confirm({
                    title: "确认删除吗？",
                    onOk() {
                        let flag = false
                        svgAllInfo[index].store.json.connections.map(item2 => {
                            if (item2.fromId === id || item2.toId === id) {
                                flag = true
                            }
                        })
                        if (flag) {
                            message.error('当前实体含有关系，请先删除相关关系！')
                        } else {
                            annotator.applyAction(Action.Label.Delete(id))
                            item.annotation = annotator.store.json
                            api.saveSvgInfo([item], taskId, paths).then(res => {
                                setRadioValue(null)
                                setRadioInputVal(null)
                            }).catch(err => {
                                canGetSvg(true)
                            })
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
            svgAllInfo[index] = annotator
            setSvgAllInfo(svgAllInfo)
        }
    }


    const radioChange = ({ target: { value } }) => {
        setRadioValue(value)
    }

    const onExpand = expandedKeys => {
        setExpandedKeys(expandedKeys)
        setAutoExpandParent(false)
    };
    const getParentKey = (key, tree) => {
        let parentKey;
        for (let i = 0; i < tree.length; i++) {
            const node = tree[i];
            if (node.children) {
                if (node.children.some(item => item.key === key)) {
                    parentKey = node.key;
                } else if (getParentKey(key, node.children)) {
                    parentKey = getParentKey(key, node.children);
                }
            }
        }
        return parentKey;
    };

    function inputSearch(value, type) {
        if (type === 'label') {
            let tempLabelArry
            if (value) {
                tempLabelArry = labelCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(labelCategories))
            }
            setRadioList(tempLabelArry)
            setRadioValue(null)
        } else if (type === 'keyWord') {
            setTreeData([])
            const expandedKeys = treeDataList
                .map(item => {
                    if (item.title.indexOf(value) > -1) {
                        return getParentKey(item.key, treeData);
                    }
                    return null;
                })
                .filter((item, i, self) => item && self.indexOf(item) === i);
            const result = loop(treeDataTemp)
            setTreeData(result)
            setExpandedKeys(expandedKeys)
            setAutoExpandParent(true)
        } else {
            let tempLabelArry
            if (value) {
                tempLabelArry = connectionCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(connectionCategories))
            }
            setRadioConnectionList(tempLabelArry)
            setRadioValue(null)
        }
    }
    // 左侧列表输入框搜索事件
    function inputChange({ target: { value } }, type) {
        if (type === 'modalLabel') {
            setNewAddLabelVal(value)
        } else {
            setKeyWord(value)
        }
    }
    // 左侧列表点击事件
    function liClick(item, index) {

    }
    // 按钮事件
    function btnClick(type) {
        if (type === 'newAddLabel') {
            setLabelAddVisible(true)
        } else if (type === 'cancel') {
            window.close()
        } else if (type === 'finish') {
            setMyModalVisible(true)
        } else if (type === 'confirmSubmit') {
            api.updateTaskStatu({
                taskId,
                stateCode: '400000'
            }).then(res => {
                message.loading('任务实例生成中...', 0)
                api.createInstance({
                    taskId
                }).then(res => {
                    message.destroy()
                    message.loading('任务实例生成成功！')
                    window.opener.location.reload();
                    setMyModalVisible(false)
                    setTimeout(() => {
                        window.close()
                    }, 500);
                })
            })
        } else if (type === 'save') {
            message.success('保存成功！')
        }
    }

    const updateConnectionAfterEdit = () => {
        let newResult
        newResult = JSON.parse(JSON.stringify(schemaLiList))
        newResult[labelIndex].text = newAddLabelVal
        api.updateConnection(newResult, taskId).then(res => {
            setSvgAllInfo({})
            getSchmaList()
            setSchemaLiList(newResult)
            setLabelAddVisible(false)
            setModalLabelInput(null)
            setLabelModalIsEdit(false)
            setNewAddLabelVal(null)
            if (paths) {
                $('.svgMainContainer').remove()
                setSvgList([])
                setCanGetSvg(true)
                setSvgSpinning(true)
                setPageCurrent(1)
            }
        }).catch(err => {

        })
    }

    const updateLabelAfterEdit = () => {
        let newResult
        newResult = JSON.parse(JSON.stringify(schemaLiList))
        newResult[labelIndex].text = newAddLabelVal
        newResult[labelIndex].color = color2
        api.updateLabel(newResult, taskId).then(res => {
            setSvgAllInfo({})
            getSchmaList()
            setSchemaLiList(newResult)
            setLabelAddVisible(false)
            setModalLabelInput(null)
            setLabelModalIsEdit(false)
            setNewAddLabelVal(null)
            if (paths) {
                $('.svgMainContainer').remove()
                setSvgList([])
                setCanGetSvg(true)
                setSvgSpinning(true)
                setPageCurrent(1)
            }
        }).catch(err => {

        })
    }
    // 弹窗确认
    function modalOk(type) {
        if (type === 'confirmNewAddLabel') {
            if (labelModalIsEdit) {
                if (newAddLabelVal) {
                    if (typeSelVal2 === '01') {
                        updateLabelAfterEdit()
                    } else {
                        updateConnectionAfterEdit()
                    }
                } else {
                    message.error('不能为空！')
                }
            } else {
                if (!newAddLabelVal) {
                    message.error('不能为空！')
                } else {
                    if (typeSelVal2 === '01') {
                        let newLabel = {
                            borderColor: color,
                            color,
                            id: Number(Math.random().toString().substr(3, 20) + Date.now()).toString(36),
                            text: newAddLabelVal
                        }
                        let newLabels = JSON.parse(JSON.stringify(labelCategories))
                        newLabels.push(newLabel)
                        api.updateLabel(newLabels, taskId).then(res => {
                            message.success('添加成功！')
                            setLabelAddVisible(false)
                            setNewAddLabelVal(null)
                            getSchmaList()
                            if (paths) {
                                setSvgAllInfo({})
                                $('.svgMainContainer').remove()
                                setSvgList([])
                                setCanGetSvg(true)
                            }
                        }).catch(err => {

                        })
                    } else {
                        let newConnection = {
                            id: Number(Math.random().toString().substr(3, 20) + Date.now()).toString(36),
                            text: newAddLabelVal
                        }
                        let newConnections = JSON.parse(JSON.stringify(connectionCategories))
                        newConnections.push(newConnection)
                        api.updateConnection(newConnections, taskId).then(res => {
                            message.success('添加成功！')
                            setLabelAddVisible(false)
                            setNewAddLabelVal(null)
                            getSchmaList()
                            if (paths) {
                                setSvgAllInfo({})
                                $('.svgMainContainer').remove()
                                setSvgList([])
                                setCanGetSvg(true)
                            }
                        }).catch(err => { })
                    }
                }
            }
        } else if (type === '1') {
            const labelId = Number(Math.random().toString().substr(3, 20) + Date.now()).toString(36)
            if (radioValue) {
                svgAllInfo[currentSvg].applyAction(Action.Label.Create(radioValue, startIndex, endIndex))
                currentSvgObj.annotation = svgAllInfo[currentSvg].store.json
                api.saveSvgInfo([currentSvgObj], taskId, paths).then(res => {
                    setVisible(false)
                    setRadioValue(null)
                    setRadioInputVal(null)
                }).catch(err => {
                    setCanGetSvg(true)
                })
            } else {
                let flag = false
                labelCategories.some(item => {
                    if (item.text === radioInputVal) {
                        flag = true
                        return
                    }
                })
                if (flag) {
                    inputSearch(radioInputVal, 'label')
                } else if (!radioValue && radioInputVal) {
                    let newLabel = {
                        borderColor: color,
                        color,
                        id: labelId,
                        text: radioInputVal
                    }
                    let newLabels = JSON.parse(JSON.stringify(labelCategories))
                    newLabels.push(newLabel)
                    api.updateLabel(newLabels, taskId).then(res => {
                        setSvgAllInfo({})
                        getSchmaList()
                        $('.svgMainContainer').remove()
                        setSvgList([])
                        setCanGetSvg(true)
                        setLabelId(labelId)
                        setVisible(false)
                        setSvgSpinning(true)
                    }).catch(err => { })
                } else {
                    message.error('请选择标签或者输入标签！')
                }
            }
        } else if (type === '2') {
            const connectionId = Number(Math.random().toString().substr(3, 20) + Date.now()).toString(36)
            if (radioValue) {
                svgAllInfo[currentSvg].applyAction(Action.Connection.Create(radioValue, first, second))
                currentSvgObj.annotation = svgAllInfo[currentSvg].store.json
                api.saveSvgInfo([currentSvgObj], taskId, paths).then(res => {
                    setVisible2(false)
                    setRadioValue(null)
                    setRadioInputVal(null)
                }).catch(err => {
                    setCanGetSvg(true)
                })
            } else {
                let flag = false
                connectionCategories.some(item => {
                    if (item.text === radioInputVal) {
                        flag = true
                        return
                    }
                })
                if (flag) {
                    inputSearch(radioInputVal, 'connection')
                } else if (!radioValue && radioInputVal) {
                    let newConnection = {
                        id: connectionId,
                        text: radioInputVal
                    }
                    let newConnections = JSON.parse(JSON.stringify(connectionCategories))
                    newConnections.push(newConnection)
                    api.updateConnection(newConnections, taskId).then(res => {
                        setSvgAllInfo({})
                        getSchmaList()
                        $('.svgMainContainer').remove()
                        setSvgList([])
                        setCanGetSvg(true)
                        setConnectionId(connectionId)
                        setVisible(false)
                        setSvgSpinning(true)
                    }).catch(err => { })
                } else {
                    message.error('请选择标签或者输入标签！')
                }
            }
        }
    }
    // 选择框事件
    function selChange(value, type) {
        if (type === 'label') {
            setTypeSelVal(value)
            setTypeSelVal2(value)
            setSchemaLiList(value === '01' ? labelCategories : connectionCategories)
        } else {
            setTypeSelVal2(value)
        }
    }
    // 右侧目录li鼠标移入移出事件
    function liMouseEvent(type, index) {
        if (type === 'enter') {
            setFocusIndex(index)
        } else if (type === 'leave') {
            setFocusIndex(null)
        }
    }
    // 右侧目录li的icon点击事件
    function iconClick(item, type, index) {
        if (type === 'edit') {
            setLabelAddVisible(true)
            setModalLabelInput(item.text)
            setLabelModalIsEdit(true)
            setNewAddLabelVal(item.text)
            setColor2(item.color)
            setLabelIndex(index)
        }
    }
    // 颜色选择
    function colorChange(value, type) {
        if (type === '1') {
            setColor(value.color)
        } else {
            setColor2(value.color)
        }
    }
    const loop = data =>
        data.map(item => {
            const index = item.title.indexOf(keyWord);
            const beforeStr = item.title.substr(0, index);
            const afterStr = item.title.substr(index + keyWord.length);
            const title =
                index > -1 ? (
                    <span>
                        {beforeStr}
                        <span className="site-tree-search-value">{keyWord}</span>
                        {afterStr}
                    </span>
                ) : (
                        <span>{item.title}</span>
                    );
            if (item.children.length > 0) {
                return { title, key: item.key, children: loop(item.children), path: item.path };
            } else {
                return {
                    title,
                    key: item.key,
                    path: item.path,
                    isLeaf: true
                };
            }
        });
    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Input.Search placeholder='请输入表名' onChange={(e) => inputChange(e, 'keyWord')} onSearch={(value) => inputSearch(value, 'keyWord')} />
                            </div>
                            <div className='mds-marking-treeWrap'>
                                <Tree.DirectoryTree
                                    defaultExpandAll
                                    showIcon
                                    onExpand={onExpand}
                                    expandedKeys={expandedKeys}
                                    treeData={treeData}
                                    onSelect={onSelect}
                                    autoExpandParent={autoExpandParent}
                                    selectedKeys={selectedKeys}
                                />
                            </div>
                        </div>
                    </Spin>
                    <div className='mds-executePage-middleWrap' >
                        <div className='mds-marking-middleWrap'>
                            <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                <div id='svgMainAllContainer'>
                                    {
                                        svgList.length > 0
                                            ?
                                            svgList.map((item, index) => {
                                                return (
                                                    renderSvg(item, index)
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </div>
                            </Spin>
                        </div>

                        <Pagination
                            className='mds-marking-svgPageNation'
                            current={pageCurrent}
                            // showQuickJumper
                            pageSize={pageSize}
                            onChange={pageChange}
                            total={pageCount}
                        />
                    </div>
                    <Spin spinning={spinning2} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-rightWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Select value={typeSelVal} onSelect={(value) => selChange(value, 'label')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                    {
                                        labelTypeList.map(item => {
                                            return (
                                                <Option value={item.code}>{item.value}</Option>
                                            )
                                        })
                                    }
                                </Select>
                                <Button type='primary' onClick={() => btnClick('newAddLabel')}>新增</Button>
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    schemaLiList.length > 0
                                        ?
                                        schemaLiList.map((item, index) => {
                                            if(item.id !== 'originId'){
                                                return (
                                                    <li
                                                        onMouseEnter={() => liMouseEvent('enter', index)}
                                                        onMouseLeave={() => liMouseEvent('leave', index)}
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        onClick={() => liClick(item, index, 'right')}
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
                                                        <span className='mds-executePage-liName'>{item.text}</span>
                                                        {
                                                            focusIndex === index
                                                                ?
                                                                <FormOutlined
                                                                    className='liIcon editIcon'
                                                                    onClick={() => iconClick(item, 'edit', index)}
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
                                                        }
                                                    </li>
                                                )
                                            }
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                        </div>
                    </Spin>
                </div>
                <div className='mds-executePage-btns' >
                    <MyButton
                        label='保存'
                        onClick={() => btnClick('save')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='提交'
                        onClick={() => btnClick('finish')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='取消'
                        classType='warm'
                        onClick={() => btnClick('cancel')}
                        style={{ marginRight: '40px' }}
                    />
                </div>
            </div>
            <Modal
                title="新增标签"
                visible={labelAddVisible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('confirmNewAddLabel')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-newAddModal'>
                    <Input.Group compact style={{ display: 'flex' }}>
                        <Select disabled={labelModalIsEdit} value={typeSelVal2} onChange={(value) => selChange(value, 'modalLabel')} placeholder='请选择' >
                            {
                                labelTypeList.map(item => {
                                    if (item.value !== '全部') {
                                        return (
                                            <Option value={item.code}>{item.value}</Option>
                                        )
                                    }
                                })
                            }
                        </Select>
                        <Input defaultValue={modalLabelInput} onChange={(e) => inputChange(e, 'modalLabel')} />
                        {
                            typeSelVal2 === '01'
                                ?
                                <ColorPicker
                                    animation="slide-up"
                                    color={color2}
                                    onChange={(value) => colorChange(value, '2')}
                                />
                                :
                                null
                        }

                    </Input.Group>
                </div>
            </Modal>
            <Modal
                title="定义标签"
                visible={visible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('1')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
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
                            <Input.Search placeholder="输入搜索标签" onSearch={value => inputSearch(value, 'label')} onChange={(e) => {
                                setRadioInputVal(e.target.value)
                            }} />
                            <ColorPicker
                                animation="slide-up"
                                defaultColor={'#1890FF'}
                                onChange={(value) => colorChange(value, '1')}
                            />
                        </Input.Group>
                        {
                            radioList.length > 0
                                ?
                                <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={radioChange} value={radioValue}>
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
                title="定义关系"
                visible={visible2}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('2')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-modalBody'>
                    <p>from：{fromLabel}</p>
                    <p>to：{endLabel}</p>
                    <div className='mds-LabelDefinition-modalConditionWrap'>
                        <Input.Search placeholder="输入搜索关系" onSearch={value => inputSearch(value, 'connection')} onChange={(e) => {
                            setRadioInputVal(e.target.value)
                        }} />
                        {
                            radioConnectionList.length > 0
                                ?
                                <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={radioChange} value={radioValue}>
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
            <MyModal
                visible={myModalVisible}
                // modalOk={modalOk}
                modalCancel={() => {
                    setMyModalVisible(false)
                }}
                title={'提示'}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => btnClick('confirmSubmit')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={() => {
                            setMyModalVisible(false)
                        }} />
                    </Fragment>

                }
            >
                <Fragment>
                    <p>提交后不可撤回，确认提交吗？</p>
                </Fragment>
            </MyModal>
        </div>
    )
}
