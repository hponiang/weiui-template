<template>
    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="back"></weiui_navbar_item>
            <weiui_navbar_item type="title">
                <text class="title">等待弹窗</text>
            </weiui_navbar_item>
            <weiui_navbar_item type="right" @click="viewCode('module/loading')">
                <weiui_icon content="md-code-working" class="iconr"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <weiui_list class="content" :weiui="{ pullTips:false }">
            <div v-for="list in sliceLists(lists, 2)" class="list">
                <div class="item" v-for="(item, index) in list" :key="index">
                    <text class="button" @click="loading(item)">样式:{{item}}</text>
                </div>
            </div>
        </weiui_list>

    </div>
</template>

<style>
    .app {
        width: 750px;
        flex: 1;
    }

    .navbar {
        width: 750px;
        height: 100px;
    }

    .title {
        font-size: 28px;
        color: #ffffff
    }

    .iconr {
        width: 100px;
        height: 100px;
        color: #ffffff;
    }

    .content {
        width: 750px;
        flex: 1;
        padding-top: 20px;
    }

    .list {
        width: 750px;
        flex-direction: row;
    }

    .item {
        width: 375px;
        align-items: center;
    }

    .button {
        width: 320px;
        font-size: 24px;
        text-align: center;
        margin-top: 30px;
        padding-top: 26px;
        padding-bottom: 26px;
        padding-left: 30px;
        padding-right: 30px;
        color: #ffffff;
        background-color: #00B4FF;
    }
</style>

<script>
    import {openViewCode} from "../statics/js/app";
    import {each} from "../statics/js/global";

    const weiui = weex.requireModule('weiui');

    export default {
        data() {
            return {
                lists: [
                    'default',
                    'RotatingPlane',
                    'DoubleBounce',
                    'Wave',
                    'WanderingCubes',
                    'Pulse',
                    'ChasingDots',
                    'ThreeBounce',
                    'Circle',
                    'CubeGrid',
                    'FadingCircle',
                    'FoldingCube',
                    'RotatingCircle'
                ],
            }
        },
        methods: {
            viewCode(str) {
                openViewCode(str);
            },
            sliceLists(data, slice) {
                let lists = [];
                for (let i = 0, len = data.length; i < len; i += slice) {
                    lists.push(data.slice(i, i + slice));
                }
                return lists;
            },
            loading(style) {
                weiui.loading({
                    style: style,
                });
            },
        }
    };
</script>
