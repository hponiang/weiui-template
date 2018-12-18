<template>
    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="back"></weiui_navbar_item>
            <weiui_navbar_item type="title">
                <text class="title">图片选择器</text>
            </weiui_navbar_item>
            <weiui_navbar_item type="right" @click="viewCode('module/third/pictureSelector')">
                <weiui_icon content="code-working" class="iconr"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <div class="content">

            <weiui_list v-if="lists.length > 0"
                        :style="{width:'750px', height: (Math.ceil(lists.length / 5) * 150) + 'px'}"
                        :weiui="{pullTips:false}">
                <div v-for="list in sliceLists(lists, 5)" class="list">
                    <div v-for="item in list" class="imgbox" @click="pictureView(item.position)">
                        <image :src="'file://' + item.path" class="image" resize="cover"></image>
                    </div>
                </div>
            </weiui_list>

            <text class="button" @click="openPicture">选择照片</text>
            <text v-if="lists.length > 0" class="button2" @click="lists=[]">清空选择</text>

        </div>

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
        flex: 1;
        justify-content: center;
        align-items: center;
    }

    .list {
        width: 750px;
        flex-direction: row;
        justify-content: center;
    }

    .imgbox {
        width: 150px;
        height: 150px;
    }

    .image {
        width: 130px;
        height: 130px;
        margin-top: 10px;
        margin-bottom: 10px;
        margin-right: 10px;
        margin-left: 10px;
    }

    .button {
        font-size: 24px;
        text-align: center;
        margin-top: 20px;
        padding-top: 20px;
        padding-bottom: 20px;
        padding-left: 48px;
        padding-right: 48px;
        color: #ffffff;
        background-color: #00B4FF;
    }

    .button2 {
        margin-top: 24px;
        color: #00B4FF;
        font-size: 24px;
        border-bottom-width: 1px;
        border-bottom-style: solid;
        border-bottom-color: #00B4FF;
    }
</style>

<script>
    import {openViewCode} from "../statics/js/app";
    import {each} from "../statics/js/global";

    const weiui_picture = weex.requireModule('weiui_picture');

    export default {
        data() {
            return {
                lists: []
            }
        },
        methods: {
            viewCode(str) {
                openViewCode(str);
            },
            sliceLists(data, slice) {
                let lists = [];
                let j = 0;
                for (let i = 0, len = data.length; i < len; i += slice) {
                    let temp = [];
                    each(data.slice(i, i + slice), (index, item) => {
                        item.position = j;
                        temp.push(item);
                        j++;
                    });
                    lists.push(temp);
                }
                return lists;
            },
            openPicture() {
                weiui_picture.create({
                    gallery: 1,
                    selected: this.lists
                }, (result) => {
                    if (result.status === "success") {
                        this.lists = result.lists;
                    }
                });
            },
            pictureView(position) {
                weiui_picture.picturePreview(position, this.lists);
            }
        }
    };
</script>
