import React, { Component, Fragment } from 'react'
import LayoutHeader from '@/components/Header';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Tree, Pagination, Radio } from 'antd';
import { MyButton, InputWrap } from '@/components/commonWrap';
import { CloseSquareOutlined } from '@ant-design/icons';
import * as api from './services';
import MyModal from '@/components/Modal'
import $ from 'jquery'
import 'rc-color-picker/assets/index.css';
import './index.less';

const { Option } = Select
let count = 0
let startIndex = 0

export default class index extends Component {

    constructor(props) {
        super(props)

        this.state = {
            taskId: null,
            spinning: true,
            sentenceSpinning: false,
            keyWord: '',
            sentence: '',
            sentencesObj: {},
            pageCurrent: 1,
            pageSize: 10,
            pageCount: 0,
            typeSelVal: null,
            treeData: [],
            treeDataTemp: [],
            paths: '',
            isLeaf: false,
            canGetSvg: true,
            expandedKeys: [],
            autoExpandParent: true,
            treeDataList: [],
            myModalVisible: false,
            indexArray: {}
        }
    }

    componentDidMount = () => {
        const { taskId } = this.props.match.params

        this.setState({
            taskId
        }, () => {
            this.getTreeData()
        })
    }


    componentDidUpdate = () => {
        if (!$('#sentenceContainerWraper').attr('isClick')) {
            $('#sentenceContainerWraper').attr('isClick', true)
            $('#sentenceContainerWraper').on('click', function (e) {
                if (
                    e.target.id === 'sentenceContainer' ||
                    e.target.className === 'tooltip' ||
                    e.target.id.indexOf('count') > -1 ||
                    e.target.nodeName === 'svg'
                ) {
                    return
                } else {
                    window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
                    $("#tooltip").remove();
                    $("#count" + count).contents().unwrap()
                }
            })
        }
    }

    // 获取左侧目录树
    getTreeData = () => {
        const { taskId } = this.state
        api.getTreeData({
            taskId
        }).then(res => {
            const { tree } = res
            const newTree = this.repalceTree(tree)
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
            generateList(newTree)
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
            this.setState({
                treeDataList: dataList,
                expandedKeys: openKeys,
                selectedKeys,
                paths,
                treeData: newTree,
                treeDataTemp: newTree,
                spinning: false
            }, () => {
                this.getSentence()
            })
        }).catch(err => {
            this.setState({
                spinning: false
            })
        })
    }

    // 组装树形数据结构
    repalceTree = (tree, node) => {
        return tree.map(item => {
            if (item.children && item.children.length > 0) {
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                this.repalceTree(item.children, item)
                return item
            } else {
                item.isLeaf = true
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                return item
            }
        })
    }

    getSentence = () => {
        const { taskId, paths } = this.state
        api.getSentence({
            taskId,
            paths
        }).then(res => {
            let newobj = JSON.parse(JSON.stringify(this.state.indexArray))
            let newSentencesObj = JSON.parse(JSON.stringify(this.state.sentencesObj))
            res.data.sentences.map((item, index) => {
                newobj['count' + index] = [parseInt(item.startIndex), parseInt(item.endIndex)]
                newSentencesObj['count' + index] = {
                    startIndex: parseInt(item.startIndex),
                    endIndex: parseInt(item.endIndex),
                    sentence: item.sentence
                }
                count += 1
                if (index === res.data.sentences.length - 1) {
                    startIndex = parseInt(item.endIndex)
                }
            })
            this.setState({
                sentence: res.data.remainSentence,
                sentencesObj: newSentencesObj,
                indexArray: newobj,
                sentenceSpinning: false
            })
        }).catch(err => {

        })
    }

    // 树节点选择
    onSelect = (selectedKeys, e) => {
        const { paths } = this.state
        if (e.node.path !== paths) {
            if (e.node.isLeaf) {
                this.setState({
                    sentenceSpinning: true,
                    sentencesObj: {},
                    indexArray: {},
                    sentence: '',
                    selectedKeys,
                    pageCurrent: 1,
                    paths: e.node.path,
                    isLeaf: e.node.isLeaf,
                }, () => {
                    startIndex = 0
                    count = 0
                    this.getSentence()
                })
            }
        }
    }

    onExpand = expandedKeys => {
        this.setState({
            expandedKeys: expandedKeys,
            autoExpandParent: false
        })
    };
    getParentKey = (key, tree) => {
        let parentKey;
        for (let i = 0; i < tree.length; i++) {
            const node = tree[i];
            if (node.children) {
                if (node.children.some(item => item.key === key)) {
                    parentKey = node.key;
                } else if (this.getParentKey(key, node.children)) {
                    parentKey = this.getParentKey(key, node.children);
                }
            }
        }
        return parentKey;
    };

    inputSearch(value, type) {
        if (type === 'keyWord') {
            this.setState({
                treeData: []
            })
            const expandedKeys = this.state.treeDataList
                .map(item => {
                    if (item.title.indexOf(value) > -1) {
                        return this.getParentKey(item.key, this.state.treeData);
                    }
                    return null;
                })
                .filter((item, i, self) => item && self.indexOf(item) === i);
            const result = this.loop(this.state.treeDataTemp)
            this.setState({
                treeData: result,
                expandedKeys: expandedKeys,
                autoExpandParent: true
            })
        }
    }
    // 左侧列表输入框搜索事件
    inputChange = ({ target: { value } }) => {
        this.setState({
            keyWord: value
        })
    }

    // 按钮事件
    btnClick(type, type2) {
        const { taskId } = this.state
        if (type === 'cancel') {
            window.close()
        } else if (type === 'finish') {
            this.setState({
                myModalVisible: true
            })
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
                    this.setState({
                        myModalVisible: false
                    })
                    setTimeout(() => {
                        window.close()
                    }, 500);
                })
            })
        } else if (type === 'save') {
            const { taskId, paths } = this.state
            const paramsObj = JSON.parse(JSON.stringify(this.state.indexArray))
            let params, indexs = []
            for (const key in paramsObj) {
                if (paramsObj.hasOwnProperty(key)) {
                    const element = paramsObj[key];
                    indexs.push(element.join('--AA--'))
                }
            }
            params = {
                taskId,
                paths,
                indexs: indexs.toString()
            }
            return new Promise((resolve, reject) => {
                api.saveSentence(params).then(res => {
                    if (!type2) {
                        message.success('保存成功！')
                    }
                    resolve(res)
                }).catch(err => {
                    reject(err)
                })
            })
        } else if (type === 'reset') {
            const _this = this
            const { taskId, paths } = this.state
            let params = {
                taskId,
                paths,
                indexs: ''
            }
            Modal.confirm({
                title: '重置分句会删除当前标注内容的所有分句，确认重置吗？',
                onOk() {
                    api.saveSentence(params).then(res => {
                        api.getSentence({
                            taskId,
                            paths
                        }).then(res2 => {
                            startIndex = 0
                            count = 0
                            _this.setState({
                                sentenceSpinning: true,
                                sentencesObj: {},
                                indexArray: {},
                                sentence: '',
                            }, () => {
                                _this.setState({
                                    sentence: res2.data.remainSentence,
                                    sentenceSpinning: false
                                })

                            })
                        })
                    }).catch(err => {
                    })
                },
                onCancel() {

                },
                cancelButtonProps: {
                    className: 'mds-btn-warm',
                    style: {
                        marginLeft: '8px'
                    }
                }
            })
        }
    }

    // pageChange = (page, pageSize) => {
    //     $('.sentenceContainer').remove()
    //     setsentenceSpinning(true)
    //     setSentence('')
    //     setPageCurrent(page)
    // }

    bindDelClick = (dom) => {
        const _this = this
        $(dom).off('contextmenu')
        $(dom).on('contextmenu', function (e) {
            const _thisDom = $(this)
            Modal.confirm({
                title: '是否删除？',
                onOk() {
                    let nextDom, nextId, id, allText
                    nextDom = _thisDom.next()
                    id = _thisDom[0].id
                    if (nextDom.length > 0 && nextDom[0].id.indexOf('count') > -1) {
                        nextId = nextDom[0].id
                        id = _thisDom[0].id
                        allText = _thisDom.text() + nextDom.text()
                        _thisDom.remove()
                        nextDom.text(allText)
                        let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                        newArray[nextId] = [newArray[id][0], newArray[nextId][1]]
                        delete newArray[id]
                        _this.setState({
                            indexArray: newArray
                        }, () => {
                            _this.bindClick(newArray)
                        })
                    } else {
                        allText = _thisDom.text()
                        _thisDom.remove()
                        const newText = allText + _this.state.sentence
                        let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                        delete newArray[id]
                        startIndex -= allText.length
                        _this.setState({
                            indexArray: newArray,
                            sentence: newText
                        }, () => {
                            // _this.bindClick(newArray)
                        })
                    }
                },
                onCancel() {

                },
                cancelButtonProps: {
                    className: 'mds-btn-warm',
                    style: {
                        marginLeft: '8px'
                    }
                }
            })
            return false
        })
    }

    bindClick = (count) => {
        if (typeof count === "number") {
            for (let index = 0; index <= count; index++) {
                this.bindDelClick(`#count${index}`)
            }
        } else {
            for (const key in count) {
                this.bindDelClick(`#${key}`)
            }
        }

    }

    divClick = (e) => {
        const _this = this
        if (e.target.nodeName === 'SPAN' && e.target.id !== 'count' + count) {
            return
        } else {
            const toolDom = $('#tooltip')
            const x = 10;
            const y = 10;
            let pos = 0;
            debugger
            if (e.target.nodeName === "DIV" || e.target.parentNode.nodeName === 'DIV') {
                pos = this.getDivPosition(e.target || e.target.parentNode);
            } else {
                pos = this.getPosition(e.target);
            }
            $("#count" + count).contents().unwrap()
            const tooltip = "<button id='tooltip' class='tooltip'>确定</button>";
            if (toolDom.length > 0) {
                $('#tooltip').remove()
            }
            $("body").append(tooltip)
            $("#tooltip").css({
                "top": (e.pageY + y) + "px",
                "left": (e.pageX + x) + "px",
                "position": "absolute"
            }).show("fast");
            let text = $('#sentenceContainer').text()
            let html = $('#sentenceContainer').html()
            let subText
            let remainingTextTmp
            let result
            if (e.target.nodeName === 'SPAN' && this.state.indexArray.hasOwnProperty(e.target.id)) {
                subText = text.substring(this.state.indexArray[e.target.id][0], pos)
                remainingTextTmp = text.substring(pos, this.state.sentence.length)
            } else if (e.target.nodeName === 'SPAN') {
                subText = text.substring(0, pos)
                remainingTextTmp = text.substring(pos, this.state.sentence.length)
            } else {
                subText = text.substring(0, pos > this.state.sentence.length ? this.state.sentence.length : pos)
                remainingTextTmp = text.substring(pos > this.state.sentence.length ? this.state.sentence.length : pos, this.state.sentence.length)
            }
            debugger
            const newHtml = `<span class="select-item" id="count${count}">${subText}</span>`


            result = newHtml + remainingTextTmp
            // const subText = e.target.nodeName === 'SPAN' ? (this.state.indexArray.hasOwnProperty(e.target.id) ? text.substring(this.state.indexArray[e.target.id][0], pos) : text.substring(startIndex, startIndex + pos)) : text.substring(startIndex, pos)


            $('#sentenceContainer').html(result)
            $("#tooltip").text('确定')
            $("#tooltip").off('click')
            $("#tooltip").on('click', function (param) {
                // $('#count' + count).css('display', 'block')
                // $('#sentenceContainer').before($('#count' + count))
                // _this.bindClick(count)
                let newSentencesObj = JSON.parse(JSON.stringify(_this.state.sentencesObj))
                let newIndexArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                newIndexArray["count" + count] = [startIndex, startIndex + pos]
                newSentencesObj["count" + count] = {
                    startIndex: startIndex,
                    endIndex: startIndex + pos,
                    sentence: subText
                }
                count += 1
                startIndex += subText.length
                $('#tooltip').remove()
                _this.setState({
                    indexArray: newIndexArray,
                    sentencesObj: newSentencesObj,
                    sentence: remainingTextTmp
                }, () => {
                    _this.btnClick('save', 'noTip').then(res => {
                        const { taskId, paths } = _this.state
                        api.getSentence({
                            taskId,
                            paths
                        }).then(res2 => {
                            _this.setState({
                                sentence: res2.data.remainSentence,
                            })
                        })
                    })
                })
            })
        }
    }
    getDivPosition = (element) => {
        let caretOffset = 0;
        const doc = element.ownerDocument || element.document;
        const win = doc.defaultView || doc.parentWindow;
        let sel;
        if (typeof win.getSelection != "undefined") {//谷歌、火狐
            sel = win.getSelection();
            if (sel.rangeCount > 0) {//选中的区域
                const range = win.getSelection().getRangeAt(0);
                let preCaretRange = range.cloneRange();//克隆一个选中区域
                preCaretRange.selectNodeContents(element);//设置选中区域的节点内容为当前节点
                preCaretRange.setEnd(range.endContainer, range.endOffset);  //重置选中区域的结束位置
                caretOffset = preCaretRange.toString().length;
            }
        } else if ((sel = doc.selection) && sel.type != "Control") {//IE
            const textRange = sel.createRange();
            let preCaretTextRange = doc.body.createTextRange();
            preCaretTextRange.moveToElementText(element);
            preCaretTextRange.setEndPoint("EndToEnd", textRange);
            caretOffset = preCaretTextRange.text.length;
        }
        return caretOffset;
    }

    loop = data =>
        data.map(item => {
            const index = item.title.indexOf(this.state.keyWord);
            const beforeStr = item.title.substr(0, index);
            const afterStr = item.title.substr(index + this.state.keyWord.length);
            const title =
                index > -1 ? (
                    <span>
                        {beforeStr}
                        <span className="site-tree-search-value">{this.state.keyWord}</span>
                        {afterStr}
                    </span>
                ) : (
                        <span>{item.title}</span>
                    );
            if (item.children.length > 0) {
                return { title, key: item.key, children: this.loop(item.children), path: item.path };
            } else {
                return {
                    title,
                    key: item.key,
                    path: item.path,
                    isLeaf: true
                };
            }
        });

    rednerSentence = (newSentencesObj) => {
        let content = []
        for (const key in newSentencesObj) {
            if (newSentencesObj.hasOwnProperty(key)) {
                content.push(<div className='confirm-select-item' id={key}>
                    <CloseSquareOutlined style={{ color: 'red' }} onClick={this.delSentence.bind(this, newSentencesObj[key], key)} />
                    {newSentencesObj[key].sentence}
                </div>)

            }
        }
        return content
    }

    delSentence(item, key, e) {
        e.stopPropagation()
        window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
        $("#tooltip").remove();
        $("#count" + count).contents().unwrap()
        const _this = this
        const _thisDom = $(`#${key}`)
        Modal.confirm({
            title: '确认删除吗？',
            onOk() {
                let nextDom, nextId, id, allText
                nextDom = _thisDom.next()
                id = _thisDom[0].id
                if (nextDom.length > 0 && nextDom[0].id.indexOf('count') > -1) {
                    nextId = nextDom[0].id
                    id = _thisDom[0].id
                    allText = _thisDom.text() + nextDom.text()
                    let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                    let newSentencesObj = JSON.parse(JSON.stringify(_this.state.sentencesObj))
                    newArray[nextId] = [newArray[id][0], newArray[nextId][1]]
                    newSentencesObj[nextId]['startIndex'] = newArray[id][0]
                    newSentencesObj[nextId]['endIndex'] = newArray[nextId][1]
                    newSentencesObj[nextId]['sentence'] = allText
                    delete newArray[id]
                    delete newSentencesObj[id]
                    _this.setState({
                        indexArray: newArray,
                        sentencesObj: newSentencesObj
                    }, () => {
                        _this.btnClick('save', 'noTip').then(res => {
                            const { taskId, paths } = _this.state
                            api.getSentence({
                                taskId,
                                paths
                            }).then(res2 => {
                                _this.setState({
                                    sentence: res2.data.remainSentence,
                                })
                            })
                        })
                    })
                } else {
                    allText = _thisDom.text()
                    const newText = allText + _this.state.sentence
                    let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                    let newSentencesObj = JSON.parse(JSON.stringify(_this.state.sentencesObj))
                    delete newSentencesObj[id]
                    delete newArray[id]
                    startIndex -= allText.length
                    _this.setState({
                        indexArray: newArray,
                        sentence: newText,
                        sentencesObj: newSentencesObj
                    }, () => {
                        _this.btnClick('save', 'noTip').then(res => {
                            const { taskId, paths } = _this.state
                            api.getSentence({
                                taskId,
                                paths
                            }).then(res2 => {
                                _this.setState({
                                    sentence: res2.data.remainSentence,
                                })
                            })
                        })
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
    }

    render() {
        const {
            expandedKeys, treeData, autoExpandParent, sentenceSpinning,
            sentence, myModalVisible, spinning, indexArray, sentencesObj,
            selectedKeys
        } = this.state
        return (
            <div className='mds-all-layout-wrap' >
                <LayoutHeader />
                <div className='mds-executePage-wrap'>
                    <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                        <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                            <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                                <div className='mds-executePage-conditionWrapL'>
                                    <Input.Search placeholder='请输入搜索名' onChange={this.inputChange} onSearch={(value) => this.inputSearch(value, 'keyWord')} />
                                </div>
                                <div className='mds-marking-treeWrap'>
                                    <Tree.DirectoryTree
                                        defaultExpandAll
                                        showIcon
                                        onExpand={this.onExpand}
                                        expandedKeys={expandedKeys}
                                        treeData={treeData}
                                        onSelect={this.onSelect}
                                        autoExpandParent={autoExpandParent}
                                        selectedKeys={selectedKeys}
                                    />
                                </div>
                            </div>
                        </Spin>
                        <div className='mds-executePage-middleWrap' >
                            <div className='mds-marking-middleWrap'>
                                <Spin style={{ height: '100%' }} spinning={sentenceSpinning}>
                                    {
                                        sentence.length === 0 && JSON.stringify(indexArray) === '{}'
                                            ?
                                            <Empty style={{ marginTop: '35px' }} />
                                            :
                                            <div id='sentenceContainerWraper'>
                                                {
                                                    this.rednerSentence(sentencesObj)
                                                }
                                                <div
                                                    className='sentence-wrapper'
                                                    id='sentenceContainer'
                                                    dangerouslySetInnerHTML={{ __html: sentence }}
                                                    onClick={(e) => {
                                                        e.stopPropagation()
                                                        this.divClick(e)
                                                    }}
                                                >
                                                </div>
                                            </div>
                                    }
                                </Spin>
                            </div>
                            {/* <Pagination
                            className='mds-marking-svgPageNation'
                            current={pageCurrent}
                            showQuickJumper
                            pageSize={pageSize}
                            onChange={pageChange}
                            total={pageCount}
                        /> */}
                        </div>
                    </div>
                    <div className='mds-executePage-btns' >
                        <MyButton
                            label='重置'
                            danger
                            onClick={() => this.btnClick('reset')}
                            style={{ marginRight: '40px' }}
                        />
                        <MyButton
                            label='保存'
                            onClick={() => this.btnClick('save')}
                            style={{ marginRight: '40px' }}
                        />
                        <MyButton
                            label='取消'
                            classType='warm'
                            onClick={() => this.btnClick('cancel')}
                            style={{ marginRight: '40px' }}
                        />
                    </div>
                </div>
                <MyModal
                    visible={myModalVisible}
                    // modalOk={modalOk}
                    modalCancel={() => {
                        this.setState({
                            myModalVisible: false
                        })
                    }}
                    title={'提示'}
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={() => this.btnClick('confirmSubmit')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={() => {
                                this.setState({
                                    myModalVisible: false
                                })
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
}