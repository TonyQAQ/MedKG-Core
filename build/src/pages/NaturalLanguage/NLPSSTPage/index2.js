import React, { Component, Fragment } from 'react'
import $ from 'jquery'
import 'webui-popover'
import './index.less'
import { Modal } from 'antd';

function isDigit(event) {
    return (event.keyCode < 0);
}

let count = 0
let startIndex = 0
let prevKey = ''

export default class index extends Component {

    constructor(props) {
        super(props)

        this.state = {
            indexArray: {}
        }
    }

    componentDidMount = () => {
        const _this = this
        $('body').click(function (e) {
            if (e.target.id === 'content1' || e.target.className === 'tooltip' || e.target.id.indexOf('count') > -1) {
                return
            } else {
                window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
                $("#tooltip").remove();
                $("#count" + count).contents().unwrap()
                // _this.bindClick(_this.state.indexArray)
            }
        })
        // $('#content1').mouseup(function (e) {
        //     e.stopPropagation()
        //     let text, range
        //     const x = 10;
        //     const y = 10;
        //     if (document.selection) {
        //         text = document.selection.createRange().text;
        //     } else if (window.getSelection()) {
        //         text = window.getSelection().toString();

        //     }

        //     if (text.length > 0) {
        //         range = window.getSelection().getRangeAt(0)
        //         const tooltip = "<button id='tooltip' class='tooltip'>确定</button>";
        //         $("body").append(tooltip)
        //         $("#tooltip").css({
        //             "top": (e.pageY + y) + "px",
        //             "left": (e.pageX + x) + "px",
        //             "position": "absolute"
        //         }).show("fast");
        //         $("#tooltip").click(function (e) {
        //             e.stopPropagation()
        //             window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
        //             // $("#tooltip").remove();

        //             const div = $('<div onselectstart="return false" onselect="document.selection.empty()" class="select-item"></div>')[0];
        //             $(div).mouseenter(function (e) {
        //                 const btn = "<div class='select-incorrect></div>"
        //                 console.log($(btn))

        //                 $(div).webuiPopover({
        //                     content: btn,
        //                     placement: 'auto',
        //                     animation: 'pop',
        //                     trigger: 'hover',
        //                     delay: {//show and hide delay time of the popover, works only when trigger is 'hover',the value can be number or object
        //                         show: 200,
        //                         hide: 200
        //                     },
        //                     width: 50,
        //                     onShow: function (param) {
        //                         param.on('click', function (param) {
        //                             $(div).webuiPopover('destroy')
        //                             $(div).contents().unwrap()

        //                         })
        //                     }
        //                 });
        //             })
        //             div.append(range.cloneContents())
        //             range.deleteContents();
        //             range.insertNode(div)
        //             window.getSelection().removeAllRanges()
        //         })
        //     } else {
        //         range = window.getSelection().getRangeAt(0)
        //         const range1 = range
        //         const start = range.startOffset
        //         const end = range.endOffset
        //         const content = range.cloneContents()
        //         const dom = $('#content1')[0]
        //         const tooltip = "<button id='tooltip' class='tooltip'>选中</button>";
        //         $("body").append(tooltip)
        //         $("#tooltip").css({
        //             "top": (e.pageY + y) + "px",
        //             "left": (e.pageX + x) + "px",
        //             "position": "absolute"
        //         }).show("fast");
        //         $("#tooltip").on('click', function (e) {
        //             debugger
        //             let text = $(dom).text()
        //             let html2 = $(dom).html()
        //             const a = text.substring(startIndex, startIndex + range.endOffset)
        //             const newHtml = "<span class='select-item' id='count" + count + "'>" + a
        //                 + "</span>"
        //             const result = html2.replace(a, newHtml)
        //             $(dom).html(result)
        //             $("#tooltip").text('确定')
        //             $("#tooltip").off('click')
        //             $("#tooltip").on('click', function (param) {
        //                 $('#count' + count).css('display', 'block')
        //                 count += 1
        //                 startIndex += a.length
        //                 $('#tooltip').remove()
        //             })
        //         })
        //     }

        // }).mousedown(function (e) {
        //     window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
        // })


        // $('#content1').on('keydown', function (param) {
        //     return
        // })
    }

    bindDelClick = (dom) => {
        const _this = this
        $(dom).off('contextmenu')
        $(dom).on('contextmenu', function (e) {
            debugger
            const _thisDom = $(this)
            Modal.confirm({
                title: '是否删除？',
                onOk() {
                    let nextDom, nextId, id, allText
                    nextDom = _thisDom.next()
                    id = _thisDom[0].id
                    if (nextDom.length > 0) {
                        nextId = nextDom[0].id
                        id = _thisDom[0].id
                        allText = _thisDom.text() + nextDom.text()
                        nextDom.contents().unwrap()
                        _thisDom.contents().unwrap()
                        let html2 = $('#content1').html()
                        const newHtml = `<span class="select-item" id="${nextId}" style="display: block;">${allText}</span>`
                        const result = html2.replace(allText, newHtml)
                        $('#content1').html(result)
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
                        _thisDom.contents().unwrap()
                        let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                        // newArray[nextId] = [newArray[id][0], newArray[nextId][1]]
                        delete newArray[id]
                        startIndex -= allText.length
                        _this.setState({
                            indexArray: newArray
                        }, () => {
                            // _this.bindClick(newArray)
                        })
                    }
                }
            })
            return false
        })
    }

    bindClick = (count) => {
        const _this = this
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
            let text = $('#content1').text()
            let html = $('#content1').html()
            let subText
            debugger
            if (e.target.nodeName === 'SPAN' && this.state.indexArray.hasOwnProperty(e.target.id)) {
                subText = text.substring(this.state.indexArray[e.target.id][0], pos)
            } else if (e.target.nodeName === 'SPAN') {
                subText = text.substring(startIndex, startIndex + pos)
            } else {
                subText = text.substring(startIndex, pos)
            }
            // const subText = e.target.nodeName === 'SPAN' ? (this.state.indexArray.hasOwnProperty(e.target.id) ? text.substring(this.state.indexArray[e.target.id][0], pos) : text.substring(startIndex, startIndex + pos)) : text.substring(startIndex, pos)
            const newHtml = `<span class="select-item" id="count${count}">${subText}</span>`
            const result = html.replace(subText, newHtml)
            $('#content1').html(result)
            if (count > 0) {
                _this.bindClick(_this.state.indexArray)
            }
            $("#tooltip").text('确定')
            $("#tooltip").off('click')
            const currentDomName = e.target.nodeName
            $("#tooltip").on('click', function (param) {
                $('#count' + count).css('display', 'block')
                _this.bindClick(count)
                let newArray = JSON.parse(JSON.stringify(_this.state.indexArray))
                newArray["count" + count] = (currentDomName === 'SPAN') ? [startIndex, startIndex + pos] : [startIndex, pos]
                count += 1
                startIndex += subText.length
                $('#tooltip').remove()
                _this.setState({
                    indexArray: newArray
                }, () => {
                    console.log(_this.state.indexArray)
                })
            })
        }
    }
    getDivPosition = function (element) {
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
                caretOffset = preCaretRange.toString().length + 1;
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

    render() {
        return (
            <div>
                <div
                    id='content1'
                    onClick={(e) => {
                        e.stopPropagation()
                        this.divClick(e)
                    }}
                >
                    2011年到了，在前几天的“2010岁末小记”中给自己定下了一个计划，其中有一条就是“每周至少写一篇技术博客。用博客的方式来督促自己学习和进步，记下学习的新知识和积累的知识点，构建自己的知识库。”。园子里高手很多，MVP就有好几位，看他们的文章真有“看君一博文，胜读四年书”之感。曾经对委托、事件云里雾里的我看了张子阳的“C#中的委托和事件”后终于明白了很多，园子里像这样的好文章还有很多，作为菜鸟我真的获益匪浅。
                    虽然自己现在水平很差，但高手都是从菜鸟成长起来的，因此我坚信只要努力学习，每天都有收获和进步，逐渐提高自己的编程水平，总有一天也能厚积薄发，写出一些比较好的博文与大家分享，帮助新手进步。作为新年第一篇博文，我打算写一个博客备份系统系列文章与园友们分享，晒晒自己的代码，非常欢迎大家提出意见和建议。
            </div>
                <span>光标位置:</span>
            </div >

        )
    }
}
