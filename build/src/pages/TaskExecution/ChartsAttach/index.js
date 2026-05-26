import React, { Component, Fragment } from 'react';
import { Button, Modal, Input, Select, Tooltip, message, Empty, Spin } from 'antd';
import jsplumb from 'jsplumb';
import pageMap from 'pagemap'
import LayoutHeader from '@/components/Header'
import { MyButton } from '@/components/commonWrap'
import MyModal from '@/components/Modal'
import { MenuUnfoldOutlined, MenuFoldOutlined, } from '@ant-design/icons';
import ColorPicker from 'rc-color-picker';
import * as api from './services'
import debounce from 'lodash/debounce';
import $ from 'jquery'
import 'rc-color-picker/assets/index.css';
import './index.less'

String.prototype.colorRgb = function () {
    // 16进制颜色值的正则
    var reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/;
    // 把颜色值变成小写
    var color = this.toLowerCase();
    if (reg.test(color)) {
        // 如果只有三位的值，需变成六位，如：#fff => #ffffff
        if (color.length === 4) {
            var colorNew = "#";
            for (var i = 1; i < 4; i += 1) {
                colorNew += color.slice(i, i + 1).concat(color.slice(i, i + 1));
            }
            color = colorNew;
        }
        // 处理六位的颜色值，转为RGB
        var colorChange = [];
        for (var i = 1; i < 7; i += 2) {
            colorChange.push(parseInt("0x" + color.slice(i, i + 2)));
        }
        return "RGB(" + colorChange.join(",") + ")";
    } else {
        return color;
    }
}

const { Option } = Select

const jsPlumbIn = jsplumb.jsPlumb;
const jsPlumbIn_common = {
    isSource: true,
    isTarget: true,
    connector: ['Bezier'],
    maxConnections: -1,              //一个端点拖拽多条连线
    endpoint: ['Dot', { radius: 5, fill: '#1890FF' }],
    endpointStyle: { fill: '#4ec2f7', radius: 5, stroke: '#37a0f4' },
    endpointHoverStyle: {
        fill: '#94dafa', stroke: '#1565C0', radius: 5
    },
    connectorStyle: { stroke: '#1890FF', strokeWidth: 3 },//设置连线的颜色以及 粗度
    connectorOverlays: [['Arrow', { width: 12, length: 12, location: -4 }]],
    connectorHoverStyle: { lineWidth: 2, stroke: 'RGBA(24, 144, 255, 0.7)', strokeStyle: '#1A32FF', outlineWidth: 10, outlineColor: '' },//鼠标悬浮到连线的样式
}




class TablePage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            allData: [],
            dataSource: [],
            dataSource2: [],
            dataSource3: [],
            dataSource4: [],
            allColumns: [],
            columns: [],
            columns2: [],
            columns3: [],
            columns4: [],
            visible: false,
            displayColorPicker: false,
            color: {
                r: '24',
                g: '144',
                b: '255',
                a: '1',
            },
            conn: null,
            currentSide: null,
            liList: [],
            liList2: [],
            rightList: [],
            rightList2: [],
            currentSelectIndexs: [],
            leftCollapsed: false,
            rightCollapsed: false,
            taskId: '',
            selVal: '',
            keyWord: '',
            tdInputVal: '',
            allDataSource: {},
            allDataColumns: {},
            tablsList: {},
            modalVisible: false,
            modalContent: '',
            publishState: '',
            modalTitle: '',
            attrName: '',
            rel: '',
            rightSpin: true,
            leftSpin: true,
            focusObj: null,
            rightAllInput: {},
            currentEdit: ''
        };
        this.debounceInitLeftList = debounce(this.debounceInitLeftList, 800)
        this.debounceInitRightList = debounce(this.debounceInitRightList, 800)
    }


    componentDidMount = () => {
        this.initMiniMap()
        const _this = this
        const { taskId } = this.props.match.params
        this.setState({
            taskId
        }, () => {
            this.initLeftList()
            this.initJsPlumbIn()
        })
    }

    initJsPlumbIn = () => {
        const _this = this
        jsPlumbIn.bind('click', function (conn, originalEvent) {
            // originalEvent.preventDefault()
            // originalEvent.stopPropagation()
            _this.deleteConnectionData(conn);

        });
        jsPlumbIn.bind('connection', function (conn, e) {//两个表进行关联时
            if (e) {
                _this.setState({
                    currentSide: jsPlumbIn.getAllConnections().slice(-1)[0],
                    conn: conn,
                    visible: true
                })
            }
        });
        jsPlumbIn.bind('connectionDetached', function (conn, e) {//断开连接时的触发事件
            if (e) {
                _this.deleteConnectionData(conn, e);
            }
        });
        jsPlumbIn.bind('beforeDrop', function dropFun(info) {
            let allLines = jsPlumbIn.getAllConnections()
            let hasSame = false
            const len = allLines.length
            for (let index = 0; index < len; index++) {
                if (allLines[index].sourceId === info.sourceId && allLines[index].targetId === info.targetId) {
                    hasSame = true
                }
            }
            if (!hasSame) {
                return true
            } else {
                return false
            }
        })
        jsPlumbIn.importDefaults({
            PaintStyle: {
                strokeWidth: 3,
                stroke: '#1890FF'
            }
        });
    }

    initRightList = () => {
        const _this = this
        const { taskId, liList } = this.state
        api.getRightList({
            taskId
        }).then(res => {
            const { data } = res
            if (data.length > 0) {
                const len = data.length
                let selectTables = []
                let newData = []
                liList.map((item, index) => {
                    for (let index2 = 0; index2 < len; index2++) {
                        const { tableMappingId, titleHead, attrList, tableName } = item
                        if (data[index2].endTableMappingId === tableMappingId || data[index2].startTableMappingId === tableMappingId) {
                            if (selectTables.indexOf(tableMappingId) < 0) {
                                attrList.map((item2) => {
                                    item2['tableMappingId'] = tableMappingId
                                })
                                const { currentSelectIndexs, allDataSource, allDataColumns } = this.state
                                let newIndexList = [...currentSelectIndexs]
                                let newAllDataSource = JSON.parse(JSON.stringify(allDataSource))
                                let newAllColumns = JSON.parse(JSON.stringify(allDataColumns))
                                $('#lineContainer2').append("<table class='drageTable' style='left:" + Math.random() * 1000 + "px;top:" + Math.random() * 600 + "px' id=" + tableMappingId + "><thead><tr><td>" + tableName + "</td></tr></thead><tbody id=tbody" + index + "></tbody></table>")
                                let ulDom = $('#tbody' + index)
                                for (let index4 = 0; index4 < attrList.length; index4++) {
                                    ulDom.append("<tr><td>" + attrList[index4].attrName + "</td></tr>")
                                }
                                let drageTables = $('.drageTable')
                                $.each(drageTables, function (indexInArray, valueOfElement) {
                                    if (!($(this).attr('hasMouseDown'))) {
                                        $(this).attr('hasMouseDown', true);
                                        $(this).on('mousedown', function () {
                                            $(this).addClass('grabbing');

                                        });
                                        $(this).on('mouseup', function () {
                                            $(this).removeClass('grabbing')
                                        });
                                    }
                                });
                                newAllDataSource[tableMappingId] = attrList
                                newAllColumns[tableMappingId] = titleHead
                                newIndexList.push(tableMappingId)
                                selectTables.push(tableMappingId)
                                this.setState({
                                    allDataSource: newAllDataSource,
                                    allDataColumns: newAllColumns,
                                    currentSelectIndexs: newIndexList
                                }, () => {
                                    jsPlumbIn.ready(function () {
                                        _this.intLineContainer(tableMappingId, tableMappingId)
                                    })
                                })
                            }

                        }

                    }
                })
                let newRightAllInput = JSON.parse(JSON.stringify(this.state.rightAllInput))
                data.map(item => {
                    newRightAllInput[item.resultId] = item.rel
                    jsPlumbIn.connect({
                        source: item.attrStart,
                        target: item.attrEnd,
                        endpoint: "Dot",
                        anchor: ['Right', 'Left'],
                    }, jsPlumbIn_common)
                    newData.push({
                        inputVal: item.rel,
                        resultIds: item.resultId,
                        endTip: item.endTip,
                        startTip: item.seartTip,
                        sourceValue: {
                            attrMappingId: item.attrStart,
                            attrName: item.startName,
                            tableMappingId: item.startTableMappingId
                        },
                        targetValue: {
                            attrMappingId: item.attrEnd,
                            attrName: item.endName,
                            tableMappingId: item.endTableMappingId
                        }
                    })
                })
                this.setState({
                    rightList: newData,
                    rightList2: newData,
                    rightAllInput: newRightAllInput,
                    rightSpin: false
                }, () => {
                    this.refreshCancas()
                })
            } else {
                this.setState({
                    rightSpin: false
                })
            }
        }).catch(err => { })
    }

    initLeftList = (keyWord) => {
        const { taskId, selVal } = this.state
        api.getLeftList({
            taskId,
            keyWord: keyWord ? keyWord : this.state.keyWord,
            isSelect: selVal
        }).then(res => {
            const { data } = res
            this.setState({
                liList: data,
                liList2: data,
                taskId,
                tableId: data.length > 0 ? data[0].tableId : '',
                keyWord: keyWord ? keyWord : this.state.keyWord,
                leftSpin: false,
                tableMappingId: data.length > 0 ? data[0].tableMappingId : '',
            }, () => {
                this.initRightList()
            })
        }).catch(err => { })
    }

    initLeftList2 = (keyWord) => {
        const { taskId, selVal } = this.state
        api.getLeftList({
            taskId,
            keyWord: keyWord ? keyWord : this.state.keyWord,
            isSelect: selVal
        }).then(res => {
            const { data } = res
            this.setState({
                liList2: data,
                taskId,
                tableId: data.length > 0 ? data[0].tableId : '',
                keyWord: keyWord ? keyWord : this.state.keyWord,
                tableMappingId: data.length > 0 ? data[0].tableMappingId : '',
            })
        }).catch(err => { })
    }

    initRdfList = () => {
        api.getRightList({
            taskId: this.state.taskId
        }).then(res2 => {
            let newData = []
            let newRightAllInput = JSON.parse(JSON.stringify(this.state.rightAllInput))
            res2.data.map(item => {
                newRightAllInput[item.resultId] = item.rel
                newData.push({
                    inputVal: item.rel,
                    resultIds: item.resultId,
                    endTip: item.endTip,
                    startTip: item.seartTip,
                    sourceValue: {
                        attrMappingId: item.attrStart,
                        attrName: item.startName,
                        tableMappingId: item.startTableMappingId
                    },
                    targetValue: {
                        attrMappingId: item.attrEnd,
                        attrName: item.endName,
                        tableMappingId: item.endTableMappingId
                    }
                })
            })
            this.setState({
                rightList: newData,
                rightList2: newData,
                rightAllInput: newRightAllInput,
                color: {
                    r: '24',
                    g: '144',
                    b: '255',
                    a: '1',
                },
            })
        })
    }



    initMiniMap = () => {
        pageMap(document.querySelector('#map'), {
            viewport: null,
            styles: {
                'table': 'rgba(0, 0, 0, 0.08)',
                'h1,a': 'rgba(0, 0, 0, 0.10)',
                'h2,h3,h4': 'rgba(0, 0, 0, 0.08)'
            },
            back: 'rgba(0, 0, 0, 0.02)',
            view: 'rgba(0, 0, 0, 0.05)',
            drag: 'rgba(0, 0, 0, 0.10)',
            interval: null
        });
    }

    intLineContainer = (table, index, tableMappingId, type) => {
        const _this = this;
        const { allDataSource } = this.state
        jsPlumbIn.setContainer("lineContainer"); //容器, 所有的连接的元素必须在容器内.
        const tableDom = document.getElementById(table);
        this.initPointId(tableDom, allDataSource[index], tableMappingId, type);
        jsPlumbIn.draggable(table, {
            stop: function (event) {
                _this.refreshCancas()
            },
            containment: 'parent'
        })
    }

    /**
    * 初始化 jsPlumbIn 端点操作,需要等待 <div id={"lineContainer"}> 内组件加载完成后执行
    * @param table
    * @param type
    */
    initPointId(table, dataSource, tableMappingId, type) {
        const trs = table.getElementsByTagName("tbody")[0].getElementsByTagName("td");
        for (let i = 0; i < trs.length; i++) {
            trs[i].setAttribute("id", dataSource[i].attrMappingId);
            trs[i].setAttribute("record", JSON.stringify(dataSource[i]));
            jsPlumbIn.makeSource(trs[i].getAttribute('id'), {
                endpoint: "Dot",
                anchor: ['Right', 'Left'],
                // connectorOverlays: [['Label', { label: 'ss', location: 0.5 }]],
            }, jsPlumbIn_common)
            // jsPlumbIn.addEndpoint(trs[i].getAttribute('id'), {
            //     anchor: ['Right']
            // }, jsPlumbIn_common);
            // jsPlumbIn.addEndpoint(trs[i].getAttribute('id'), {
            //     anchor: ['Left']
            // }, jsPlumbIn_common);
        }
        if (type === 'new') {
            this.state.rightList.map(item => {
                if (item.sourceValue.tableMappingId === tableMappingId || item.targetValue.tableMappingId === tableMappingId) {
                    let allLines = jsPlumbIn.getAllConnections()
                    let isSame = false
                    const len = allLines.length
                    for (let index = 0; index < len; index++) {
                        if (allLines[index].sourceId === item.sourceValue.attrMappingId && allLines[index].targetId === item.targetValue.attrMappingId) {
                            isSame = true
                        }
                    }
                    if (!isSame) {
                        jsPlumbIn.connect({
                            source: item.sourceValue.attrMappingId,
                            target: item.targetValue.attrMappingId,
                            endpoint: "Dot",
                            anchor: ['Right', 'Left'],
                        }, jsPlumbIn_common)
                    }
                }
            })
        } else {
            this.state.rightList.map(item => {
                if (item.sourceValue.tableMappingId !== tableMappingId && item.targetValue.tableMappingId !== tableMappingId) {
                    let allLines = jsPlumbIn.getAllConnections()
                    let isSame = false
                    const len = allLines.length
                    for (let index = 0; index < len; index++) {
                        if (allLines[index].sourceId === item.sourceValue.attrMappingId && allLines[index].targetId === item.targetValue.attrMappingId) {
                            isSame = true
                        }
                    }
                    if (!isSame) {
                        jsPlumbIn.connect({
                            source: item.sourceValue.attrMappingId,
                            target: item.targetValue.attrMappingId,
                            endpoint: "Dot",
                            anchor: ['Right', 'Left'],
                        }, jsPlumbIn_common)
                    }
                }
            })
        }
    }

    /**
     * 删除连线钱数据处理操作,可用 connectionDetached() 代替该函数
     * @param e
     */
    deleteConnectionData = (conn, e) => {
        this.unBuildConnectionData(conn, e);
    }

    /**
     * 连线完成数据处理操作
     * @param conn
     */
    connection = (conn) => {
        //TODO: 连线完成需要执行的操作
        // this.state.currentSide.setLabel({ label: 'ss', cssClass: 'overlay-label' })
        conn.connection.setHoverPaintStyle({ stroke: `rgba(${this.state.color.r}, ${this.state.color.g}, ${this.state.color.b}, 0.7)` })
        conn.connection.setPaintStyle({ stroke: `rgba(${this.state.color.r}, ${this.state.color.g}, ${this.state.color.b}, ${this.state.color.a})` })
        this.buildConnectionData(conn);
    }

    /**
     * 构建连线 source 和 target 的service数据操作接口
     * @param conn
     */
    buildConnectionData = (conn) => {
        //TODO:连线完成需要执行的操作,elementID 是jsPlumbIn的操作信息,用于删除连线时候的数据处理
        let result = JSON.parse(JSON.stringify(this.state.rightList))
        const sourceValue = JSON.parse(document.getElementById(conn.sourceId).getAttribute('record'))
        sourceValue.elementID = conn.sourceId;
        const targetValue = JSON.parse(document.getElementById(conn.targetId).getAttribute('record'))
        targetValue.elementID = conn.targetId;
        result.push({ sourceValue, targetValue, inputVal: this.state.tdInputVal })
        const { taskId } = this.state
        api.saveConnection({
            resultId: "",
            taskId,
            attrStart: sourceValue.attrMappingId,
            attrEnd: targetValue.attrMappingId,
            attrUnique: "",
            rel: this.state.tdInputVal
        }).then(res => {
            this.initRdfList()
        }).catch(err => {
            jsPlumbIn.deleteConnection(conn);
        })
    }

    /**
     * 删除连线 source 和 target 的service数据操作接口
     * @param conn
     */
    unBuildConnectionData = (conn, e) => {
        const list = JSON.parse(JSON.stringify(this.state.rightList))
        list.map(item => {
            if (item.sourceValue.attrMappingId === conn.sourceId && item.targetValue.attrMappingId === conn.targetId) {
                this.deleteClick(item, conn, e)
            }
        })
    }

    /**
     * 连线断开数据处理操作,如果是删除操作触发,删除操作接口调用完成后,调用该接口
     * 如果是拖拽导致连线断开,将直接调用该接口,该接口类似删除接口.
     * @param conn
     */
    connectionDetached = (conn) => {
        this.unBuildConnectionData(conn);
    }


    handleClick = () => {
        this.setState({ displayColorPicker: !this.state.displayColorPicker })
    };

    handleClose = () => {
        this.setState({ displayColorPicker: false })
    };

    handleChange = (color) => {
        this.setState({ color: color.rgb })
    };

    handleOk = () => {
        const { tdInputVal } = this.state
        if (tdInputVal) {
            this.setState({
                visible: false,
                tdInputVal: ''
            })
            this.connection(this.state.conn);
        } else {
            message.error('不能为空！')
        }
    }

    handleCancel = () => {
        this.setState({
            visible: false
        })
        this.deleteConnectionData(this.state.conn)
        jsPlumbIn.deleteConnection(this.state.currentSide);
    }

    selChange(type, value) {
        if (type === 'leftSel') {
            this.setState({
                selVal: value
            }, () => {
                this.initLeftList2()
            })
        }
    }


    debounceInitLeftList = () => {
        this.initLeftList2()
    }

    debounceInitRightList = () => {
        this.initRightList2()
    }

    initRightList2 = () => {
        const { taskId, attrName, rel } = this.state
        api.getRightList({
            taskId,
            attrName,
            rel
        }).then(res => {
            let newData = []
            res.data.map(item => {
                newData.push({
                    inputVal: item.rel,
                    resultIds: item.resultId,
                    endTip: item.endTip,
                    startTip: item.seartTip,
                    sourceValue: {
                        attrMappingId: item.attrStart,
                        attrName: item.startName,
                        tableMappingId: item.startTableMappingId
                    },
                    targetValue: {
                        attrMappingId: item.attrEnd,
                        attrName: item.endName,
                        tableMappingId: item.endTableMappingId
                    }
                })
            })
            this.setState({
                rightList2: newData
            })
        })
    }

    inputChange(type, { target: { value } }) {
        if (type === 'tableName') {
            this.setState({
                keyWord: value
            }, () => {
                this.debounceInitLeftList()
            })
        } else if (type === 'attr') {
            this.setState({
                attrName: value
            }, () => {
                this.debounceInitRightList()
            })
        } else if (type === 'relation') {
            this.setState({
                rel: value
            }, () => {
                this.debounceInitRightList()
            })
        }
    }


    // 左侧表选择
    liClick(item, index) {
        const _this = this
        const { tableMappingId, titleHead, attrList, tableName } = item
        attrList.map(item => {
            item['tableMappingId'] = tableMappingId
        })
        const { currentSelectIndexs, allDataSource, allDataColumns, tablsList, rightList } = this.state
        let newIndexList = [...currentSelectIndexs]
        let newAllDataSource = JSON.parse(JSON.stringify(allDataSource))
        let newAllColumns = JSON.parse(JSON.stringify(allDataColumns))
        if (newIndexList.indexOf(tableMappingId) > -1) {
            // jsPlumbIn.reset()
            jsPlumbIn.empty(tableMappingId)
            jsPlumbIn.remove(tableMappingId)

            // this.initJsPlumbIn()
            delete newAllDataSource[tableMappingId]
            delete newAllColumns[tableMappingId]
            delete tablsList[tableMappingId]
            newIndexList.splice(newIndexList.indexOf(tableMappingId), 1)
            newIndexList.map(item => {
                // this.intLineContainer(item, item, tableMappingId)
            })
            this.setState({
                tablsList,
                allDataSource: newAllDataSource,
                allDataColumns: newAllColumns,
                currentSelectIndexs: newIndexList
            }, () => {
                this.refreshCancas()
            })
        } else {
            $('#lineContainer2').append("<table class='drageTable' style='left:" + Math.random() * 1000 + "px;top:" + Math.random() * 600 + "px' id=" + tableMappingId + "><thead><tr><td>" + tableName + "</td></tr></thead><tbody id=tbody" + index + "></tbody></table>")
            let ulDom = $('#tbody' + index)
            for (let index = 0; index < attrList.length; index++) {
                ulDom.append("<tr><td>" + attrList[index].attrName + "</td></tr>")
            }
            $.each($('.drageTable'), function (indexInArray, valueOfElement) {
                if (!($(this).attr('hasMouseDown'))) {
                    $(this).attr('hasMouseDown', true);
                    $(this).on('mousedown', function () {
                        $(this).addClass('grabbing');

                    });
                    $(this).on('mouseup', function () {
                        $(this).removeClass('grabbing')
                    });
                }
            });
            newAllDataSource[tableMappingId] = attrList
            newAllColumns[tableMappingId] = titleHead
            newIndexList.push(tableMappingId)
            this.setState({
                allDataSource: newAllDataSource,
                allDataColumns: newAllColumns,
                currentSelectIndexs: newIndexList
            }, () => {
                jsPlumbIn.ready(function () {
                    _this.intLineContainer(tableMappingId, tableMappingId, tableMappingId, 'new')
                })
                this.refreshCancas()
            })

        }
    }

    refreshCancas = (type) => {
        const c = document.getElementById("map");
        c.remove()
        var canvasObj = document.createElement("canvas");
        canvasObj.setAttribute("id", "map");
        const fatherObj = document.getElementById("lineContainer2");
        if (this.state.rightCollapsed) {
            canvasObj.style.right = "0px";
        } else {
            canvasObj.style.right = "400px";
        }
        fatherObj.append(canvasObj)
        this.initMiniMap()
    }

    toggle(type) {
        if (type === 'left') {
            this.setState({
                leftCollapsed: !this.state.leftCollapsed,
            }, () => {
                this.refreshCancas('left')
            });
        } else {
            this.setState({
                rightCollapsed: !this.state.rightCollapsed,
            }, () => {
                this.refreshCancas('right')
            });
        }
    };

    tdInputFocus(item) {
        this.setState({
            focusObj: item,
            currentEdit: item.resultIds
        })
    }

    tdInputOnBlur(item) {
        this.setState({
            currentEdit: '',
            rightSpin: true
        }, () => {
            const { focusObj: { resultIds, sourceValue: { attrMappingId }, targetValue: { attrMappingId: attrEnd } }, taskId, rightAllInput } = this.state
            api.saveConnection({
                resultId: resultIds,
                taskId,
                attrStart: attrMappingId,
                attrEnd,
                attrUnique: "",
                rel: rightAllInput[resultIds]
            }).then(res => {
                setTimeout(() => {
                    this.setState({
                        rightSpin: false
                    })
                }, 500);
            }).catch(err => {
                message.error('保存失败！')
                this.setState({
                    rightSpin: false
                })
            })
        })
    }

    tdInputChange = (type, record, { target: { value } }) => {
        if (type === 'modal') {
            this.setState({
                tdInputVal: value
            })
        } else {
            let newRightAllInput = JSON.parse(JSON.stringify(this.state.rightAllInput))
            newRightAllInput[record.resultIds] = value
            this.setState({
                rightAllInput: newRightAllInput
            })
        }

    }

    deleteClick(record, conn, e, type) {
        const _this = this
        Modal.confirm({
            title: '确认删除吗？',
            onOk() {
                api.deleteRDF({
                    resultIds: record.resultIds
                }).then(res => {
                    api.getRightList({
                        taskId: _this.state.taskId
                    }).then(res => {
                        let newData = []
                        if (type === 'rightClick') {
                            const allConnections = jsPlumbIn.getAllConnections()
                            let result = null
                            const len = allConnections.length
                            for (let index = 0; index < len; index++) {
                                if (allConnections[index].sourceId === (record.sourceValue.elementID || record.sourceValue.attrMappingId) && allConnections[index].targetId === (record.targetValue.elementID || record.targetValue.attrMappingId)) {
                                    result = allConnections[index]
                                    break;
                                }
                            }
                            jsPlumbIn.deleteConnection(result);
                            res.data.map(item => {
                                newData.push({
                                    inputVal: item.rel,
                                    resultIds: item.resultId,
                                    sourceValue: {
                                        attrMappingId: item.attrStart,
                                        attrName: item.startName,
                                        tableMappingId: item.startTableMappingId
                                    },
                                    targetValue: {
                                        attrMappingId: item.attrEnd,
                                        attrName: item.endName,
                                        tableMappingId: item.endTableMappingId
                                    }
                                })
                            })
                            _this.setState({
                                rightList: newData,
                                rightList2: newData,
                            })
                        } else {
                            if (!e) {
                                jsPlumbIn.deleteConnection(conn);
                            }
                            res.data.map(item => {
                                newData.push({
                                    inputVal: item.rel,
                                    resultIds: item.resultId,
                                    sourceValue: {
                                        attrMappingId: item.attrStart,
                                        attrName: item.startName,
                                        tableMappingId: item.startTableMappingId
                                    },
                                    targetValue: {
                                        attrMappingId: item.attrEnd,
                                        attrName: item.endName,
                                        tableMappingId: item.endTableMappingId
                                    }
                                })
                            })
                            _this.setState({
                                rightList: newData,
                                rightList2: newData,
                            })
                        }

                    })
                }).catch(err => { })
            },
            onCancel() {
                if (e) {
                    jsPlumbIn.connect({
                        source: conn.sourceId,
                        target: conn.targetId,
                        endpoint: "Dot",
                        anchor: ['Right', 'Left'],
                    }, jsPlumbIn_common)
                }
            },
            cancelButtonProps: {
                className: 'mds-btn-warm',
                style: {
                    marginLeft: '8px'
                }
            }
        })
    }

    colorChange = (value) => {
        const newColor = value.color.colorRgb()
        const rt = /(.+)?(?:\(|（)(.+)(?=\)|）)/.exec(newColor)
        const colorResult = rt[2].split(',')
        this.setState({
            strokeColoc: newColor,
            color: {
                r: colorResult[0],
                g: colorResult[1],
                b: colorResult[2],
                a: '1',
            },
        })
    }

    btnsClick(type) {
        switch (type) {
            case 'cancel': {
                window.close()
                break;
            }
            case 'save': {
                message.success('保存成功！')
                break;
            }
            case 'submit': {
                const { taskId } = this.state
                api.confirmSubmitInfo({
                    taskId
                }).then(res => {
                    const { publishComfirm, publishState } = res.data
                    this.setState({
                        modalContent: publishComfirm,
                        publishState,
                        modalVisible: true,
                        modalTitle: ('发布')
                    })
                }).catch(err => { })
                break;
            }
        }
    }

    confirmClick = () => {
        const { taskId } = this.state
        api.confirmSubmit({
            taskId
        }).then(res => {
            message.success('提交成功，窗口即将关闭！')
            window.opener.location.reload();
            setTimeout(() => {
                window.close()
            }, 1000);
        })
    }

    render() {
        const {
            liList2, currentSelectIndexs, leftCollapsed, rightCollapsed, visible, modalVisible, rightList2,
            modalContent, publishState, modalTitle, rightSpin, leftSpin, rightAllInput, currentEdit
        } = this.state
        return (
            <div className='mds-all-layout-wrap' >
                <LayoutHeader style={{
                    position: 'fixed',
                    top: '0',
                    width: '100%',
                    zIndex: '1'
                }} />
                <div className='mds-chartsAttach-contentWrap'>
                    <div className='mds-executePage-leftWrap' id='tooltipWrap' style={{
                        position: 'fixed',
                        left: '0',
                        top: '70px',
                        zIndex: '1',
                        backgroundColor: 'white',
                        width: leftCollapsed ? '0' : '250px',
                        minWidth: leftCollapsed ? '0' : '250px'
                    }}>
                        <Spin spinning={leftSpin} tip="加载中...">
                            <div className='mds-executePage-conditionWrapL'>
                                <Select
                                    onSelect={this.selChange.bind(this, 'leftSel')}
                                    placeholder='请选择'
                                    style={{
                                        width: '150px',
                                        marginRight: '5px',
                                        display: leftCollapsed ? 'none' : 'block'
                                    }}>
                                    <Option value='all'>全部</Option>
                                    <Option value='yes'>已选择</Option>
                                    <Option value='no'>未选择</Option>
                                </Select>
                                <Input
                                    style={{
                                        display: leftCollapsed ? 'none' : 'block'
                                    }}
                                    placeholder='请输入表名'
                                    onChange={this.inputChange.bind(this, 'tableName')}
                                />
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    liList2.length > 0
                                        ?
                                        liList2.map((item, index) => {
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
                                                        onClick={this.liClick.bind(this, item, index)}
                                                        className={
                                                            currentSelectIndexs.indexOf(item.tableMappingId) > -1
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
                            {React.createElement(leftCollapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
                                className: 'charts-trigger',
                                onClick: this.toggle.bind(this, 'left')
                            })}
                        </Spin>
                    </div>
                    <div
                        className='mds-chartsAttach-middleWrap'
                        style={{
                            marginLeft: leftCollapsed ? '0' : '260px',
                            marginRight: rightCollapsed ? '0' : '400px'
                        }}
                        id={"lineContainer"}
                    >
                        <div className='mds-chartsAttach-middlContent' id={"lineContainer2"} >
                            <canvas id='map'></canvas>
                            <Modal
                                title="属性关联"
                                visible={visible}
                                destroyOnClose
                                // onOk={this.handleOk}
                                onCancel={this.handleCancel}
                                footer={
                                    <Fragment>
                                        <Button type={'primary'} onClick={this.handleOk}>确定</Button>
                                        <MyButton label='取消' classType='warm' onClick={this.handleCancel} />
                                    </Fragment>
                                }
                                width='40%'
                            >
                                <table className='modal-table-wrap'>
                                    <thead>
                                        <tr>
                                            <td>
                                                属性
                                            </td>
                                            <td>
                                                关系
                                            </td>
                                            <td>
                                                属性
                                            </td>
                                            <td>
                                                颜色
                                            </td>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td className='td-text'>
                                                <Tooltip title={this.state.conn ? this.state.conn.source.innerHTML : ''}>
                                                    {this.state.conn ? this.state.conn.source.innerHTML : ''}
                                                </Tooltip>
                                            </td>
                                            <td className='td-input'>
                                                <Input onChange={this.tdInputChange.bind(this, 'modal', '')} />
                                            </td>
                                            <td className='td-text'>
                                                <Tooltip title={this.state.conn ? this.state.conn.target.innerHTML : ''}>
                                                    {this.state.conn ? this.state.conn.target.innerHTML : ''}
                                                </Tooltip>
                                            </td>
                                            <td>
                                                <ColorPicker
                                                    animation="slide-up"
                                                    defaultColor={'#1890FF'}
                                                    onChange={this.colorChange}
                                                />
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </Modal>
                        </div>
                    </div>
                    <div
                        className='mds-chartsAttach-rightWrap'
                        style={{
                            width: rightCollapsed ? '0' : '400px',
                            minWidth: rightCollapsed ? '0' : '400px',
                        }}
                    >
                        <Spin tip={'加载中...'} spinning={rightSpin} style={{ height: '100%' }}>
                            {
                                currentEdit
                                    ?
                                    <div className='mds-right-mask'></div>
                                    :
                                    null
                            }
                            <div className='mds-executePage-conditionWrapL'>
                                <Input
                                    style={{
                                        display: rightCollapsed ? 'none' : 'block',
                                        marginRight: rightCollapsed ? '0' : '5px',
                                    }}
                                    placeholder='请输入属性'
                                    onChange={this.inputChange.bind(this, 'attr')}
                                />
                                <Input
                                    style={{
                                        display: rightCollapsed ? 'none' : 'block'
                                    }}
                                    placeholder='请输入关系'
                                    onChange={this.inputChange.bind(this, 'relation')}
                                />
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                <div className='mds-rightList-head'>
                                    <span>属性</span>
                                    <span>关系</span>
                                    <span>属性</span>
                                    <span>操作</span>
                                </div>
                                {
                                    rightList2.length > 0
                                        ?
                                        rightList2.map((item, index) => {
                                            return (
                                                <div className='mds-rightList-son'>
                                                    <Tooltip title={item.startTip}>
                                                        <span>
                                                            {item.sourceValue.attrName}
                                                        </span>
                                                    </Tooltip>
                                                    <span className={currentEdit === item.resultIds ? 'td-input td-inputCurrent' : 'td-input'}>
                                                        <Input
                                                            value={rightAllInput[item.resultIds]}
                                                            onChange={this.tdInputChange.bind(this, 'right', item)}
                                                            onFocus={this.tdInputFocus.bind(this, item)}
                                                            onBlur={this.tdInputOnBlur.bind(this, item)}
                                                        />
                                                    </span>
                                                    <Tooltip title={item.endTip}>
                                                        <span title={item.targetValue.attrName}>
                                                            {item.targetValue.attrName}
                                                        </span>
                                                    </Tooltip>
                                                    <span>
                                                        <Button danger type='primary' onClick={this.deleteClick.bind(this, item, '', '', 'rightClick')}>删除</Button>
                                                    </span>
                                                </div>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                            <div className='mds-rightList-btns' style={{
                                display: rightCollapsed ? 'none' : 'flex'
                            }}>
                                <Button type='primary' onClick={this.btnsClick.bind(this, 'save')}>保存</Button>
                                <Button type='primary' onClick={this.btnsClick.bind(this, 'submit')}>提交</Button>
                                <MyButton classType='warm' label='取消' onClick={this.btnsClick.bind(this, 'cancel')} />
                            </div>
                            {React.createElement(!rightCollapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
                                className: 'charts-trigger',
                                onClick: this.toggle.bind(this, 'right'),
                                style: rightCollapsed ? {
                                    position: 'fixed',
                                    right: '0',
                                    bottom: '0'
                                }
                                    :
                                    null
                            })}
                        </Spin>
                    </div>
                </div>
                <MyModal
                    visible={modalVisible}
                    // modalOk={modalOk}
                    modalCancel={() => {
                        this.setState({
                            modalVisible: false
                        })
                    }}
                    title={modalTitle}
                    footer={
                        publishState === '01'
                            ?
                            <Button type={'primary'} onClick={() => {
                                this.setState({
                                    modalVisible: false
                                })
                            }}>
                                {'确定'}
                            </Button>
                            :
                            <Fragment>
                                <Button type={'primary'} onClick={this.confirmClick}>确定</Button>
                                <MyButton label='取消' classType='warm' onClick={() => {
                                    this.setState({
                                        modalVisible: false
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
        );
    }
}

export default TablePage