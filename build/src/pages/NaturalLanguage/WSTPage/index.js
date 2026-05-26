import React, { Component, Fragment } from 'react'
import LayoutHeader from '@/components/Header';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Tree, Pagination, Radio } from 'antd';
import { MyButton, InputWrap } from '@/components/commonWrap';
import { CloseSquareOutlined } from '@ant-design/icons';
import * as api from './services';
import MyModal from '@/components/Modal'
import $ from 'jquery'
import Highlighter from 'web-highlighter';
import 'rc-color-picker/assets/index.css';
import './index.less';

const { Option } = Select
let count = 0
let startIndex = 0
let highlighterObjs = {}

function getUuid() {
    let s = [];
    const hexDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for (let i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1)
    }
    s[14] = "4"
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1)
    s[8] = s[13] = s[18] = s[23] = "-"
    let uuid = s.join("")
    return uuid
}

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
            indexArray: {},
            sentences: [],
            selectedKeys: []
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
                this.getWstList()
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

    getWstList = () => {
        const { taskId, paths, pageCurrent, pageSize } = this.state
        api.getWstList({
            taskId,
            paths,
            pageCount: pageCurrent,
            pageSize
        }).then(res => {
            const newSentences = res.data.sentences.map(item => {
                item.words.map(wordItem =>
                    wordItem.id = getUuid()
                )
                return item
            })
            this.setState({
                sentenceSpinning: false,
                sentences: newSentences,
                pageCount: res.totalCount
            })
        }).catch(err => {

        })
    }

    // 树节点选择
    onSelect = (selectedKeys, e) => {
        const { paths } = this.state
        if (e.node.path !== paths) {
            if (e.node.isLeaf) {
                for (const key in highlighterObjs) {
                    if (highlighterObjs.hasOwnProperty(key)) {
                        const element = highlighterObjs[key];
                        element.stop()
                        element.dispose()
                    }
                }
                $('#sentenceAllContainerWraper').children('.sentenceContainerWraper').remove()
                highlighterObjs = {}
                this.setState({
                    sentenceSpinning: true,
                    sentencesObj: {},
                    selectedKeys,
                    indexArray: {},
                    sentences: [],
                    pageCurrent: 1,
                    paths: e.node.path,
                    isLeaf: e.node.isLeaf,
                }, () => {
                    startIndex = 0
                    count = 0
                    this.getWstList()
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
    inputChange({ target: { value } }, type) {
        this.setState({
            keyWord: value
        })
    }

    // 按钮事件
    btnClick(type) {
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
            const { taskId, paths, sentences } = this.state
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
            const newSentences = JSON.parse(JSON.stringify(sentences))
            for (let index = 0; index < newSentences.length; index++) {
                let element = newSentences[index];
                element.words.map(item => {
                    delete item.text
                })
            }
            api.saveWords(
                newSentences,
                this.state.taskId,
                this.state.paths).then(res => {
                    message.success('保存成功！')
                }).catch(err => { })

        }
    }

    pageChange = (page, pageSize) => {
        for (const key in highlighterObjs) {
            if (highlighterObjs.hasOwnProperty(key)) {
                const element = highlighterObjs[key];
                element.stop()
                element.dispose()
            }
        }
        $('#sentenceAllContainerWraper').empty()
        highlighterObjs = {}
        this.setState({
            pageCurrent: page,
            sentenceSpinning: true,
            sentences: []
        }, () => {
            this.getWstList()
        })
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

    renderSentenceAgain = (item, index) => {
        const len = $('#sentenceAllContainerWraper').children("#sentenceContainerWraper" + index).length
        if (len === 0) {
            $('#sentenceAllContainerWraper').append("<div class='sentenceContainerWraper' id='sentenceContainerWraper" + index + "'></div>")
            $(`#sentenceContainerWraper${index}`).text(item.content)
        }
        let wrapDom = $(`#sentenceContainerWraper${index}`)[0]
        if (wrapDom && $(wrapDom).children().length === 0) {
            $(wrapDom).text(item.content)
            const highlighter = new Highlighter({
                $root: wrapDom,
                style: {
                    className: 'highlight-my-wrap'
                }
            })
            highlighterObjs[index] = highlighter
            item.words.map(wordItem => {
                highlighterObjs[index].fromStore(
                    {
                        textOffset: wordItem.startIndex,
                        parentIndex: -2,
                        parentTagName: "DIV"
                    },
                    {
                        textOffset: wordItem.endIndex,
                        parentIndex: -2,
                        parentTagName: "DIV"
                    },
                    wordItem.text,
                    wordItem.id
                )
            })
            $(wrapDom).children('span').each(function (param) {
                if ($(this).html() === '') {
                    $(this).remove()
                }
            })
            highlighterObjs[index].on('selection:create', ({ sources }) => {
                sources.map(sourceItem => {
                    if (sourceItem.endMeta.parentIndex !== sourceItem.startMeta.parentIndex) {
                        highlighterObjs[index].remove(sourceItem.id)
                    } else {
                        let flag = false
                        let currentWord
                        for (let index2 = 0; index2 < this.state.sentences[index].words.length; index2++) {
                            const wordItem = this.state.sentences[index].words[index2];
                            if (wordItem.startIndex > sourceItem.startMeta.textOffset && wordItem.startIndex < sourceItem.endMeta.textOffset) {
                                flag = true
                                // highlighterObjs[item.sentenceIndex].remove(wordItem.id)
                                currentWord = wordItem
                                break
                            } else if (wordItem.endIndex > sourceItem.startMeta.textOffset && wordItem.startIndex < sourceItem.endMeta.textOffset) {
                                flag = true
                                // highlighterObjs[item.sentenceIndex].remove(wordItem.id)
                                currentWord = wordItem
                                break
                            }
                        }
                        if (!flag) {
                            let result = [...this.state.sentences]
                            result[index].words.push({
                                endIndex: sourceItem.endMeta.textOffset,
                                id: sourceItem.id,
                                startIndex: sourceItem.startMeta.textOffset,
                                text: sourceItem.text
                            })
                            this.setState({
                                sentences: result
                            })
                            // api.saveWords([{
                            //     sentenceIndex: item.sentenceIndex,
                            //     content: item.content,
                            //     words: [...this.state.sentences[index].words, {
                            //         id: sourceItem.id,
                            //         startIndex: sourceItem.startMeta.textOffset,
                            //         endIndex: sourceItem.endMeta.textOffset
                            //     }]
                            // }],
                            //     this.state.taskId,
                            //     this.state.paths
                            // ).then(res => {
                            //     let result = [...this.state.sentences]
                            //     result[index].words.push({
                            //         endIndex: sourceItem.endMeta.textOffset,
                            //         id: sourceItem.id,
                            //         startIndex: sourceItem.startMeta.textOffset,
                            //         text: sourceItem.text
                            //     })
                            //     this.setState({
                            //         sentences: result
                            //     })
                            // }).catch(err => {
                            //     highlighterObjs[index].remove(sourceItem.id)
                            // })
                        } else {
                            // highlighterObjs[item.sentenceIndex].removeAll()
                            console.log(111)
                            highlighterObjs[index].dispose()
                            highlighterObjs[index] = null
                            $(wrapDom).empty()
                            this.renderSentenceAgain(this.state.sentences[index], index)
                        }
                    }
                })
            })
            highlighterObjs[index].on('selection:hover', ({ id }) => {
                highlighterObjs[index].addClass('highlight-wrap-hover', id);
            })
            highlighterObjs[index].on('selection:hover-out', ({ id }) => {
                highlighterObjs[index].removeClass('highlight-wrap-hover', id);
            })
            highlighterObjs[index].on('selection:click', ({ id }) => {
                let resultObj = {
                    ...this.state.sentences[index],
                    words: this.state.sentences[index].words.filter(wordItem => {
                        if (wordItem.id !== id) {
                            return this.state.sentences[index]
                        }
                    })
                }
                highlighterObjs[index].remove(id);
                let result = [...this.state.sentences]
                result[index] = resultObj
                this.setState({
                    sentences: result
                })
                // api.saveWords([resultObj],
                //     this.state.taskId,
                //     this.state.paths
                // ).then(res => {
                //     highlighterObjs[index].remove(id);
                //     let result = [...this.state.sentences]
                //     result[index] = resultObj
                //     this.setState({
                //         sentences: result
                //     })
                // }).catch(err => {
                // })
            })
            highlighterObjs[index].run();
        }
    }

    rednerSentence = (item, index) => {
        this.renderSentenceAgain(item, index)
    }

    render() {
        const {
            expandedKeys, treeData, autoExpandParent, sentenceSpinning,
            myModalVisible, spinning, pageCurrent, pageCount, pageSize, sentences,
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
                                    <Input.Search placeholder='请输入表名' onChange={(e) => this.inputChange(e, 'keyWord')} onSearch={(value) => this.inputSearch(value, 'keyWord')} />
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
                                    <div id='sentenceAllContainerWraper'>
                                        {
                                            sentences.length > 0 && !sentenceSpinning
                                                ?
                                                sentences.map((item, index) => {
                                                    this.rednerSentence(item, index)
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
                                showQuickJumper
                                pageSize={pageSize}
                                onChange={this.pageChange}
                                total={pageCount}
                            />
                        </div>
                    </div>
                    <div className='mds-executePage-btns' >
                        <MyButton
                            label='保存'
                            onClick={() => this.btnClick('save')}
                            style={{ marginRight: '40px' }}
                        />
                        {/* <MyButton
                            label='提交'
                            onClick={() => this.btnClick('finish')}
                            style={{ marginRight: '40px' }}
                        /> */}
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