#!/bin/bash
workdir=$(
    cd $(dirname $0)
    pwd
)
zipDir=$1
rn=$2

setprop persist.logd.size 8388608

if [ ! -f "/data/7za" ]; then
    cp -r "$workdir"/7za /data
    chmod 777 /data/7za
else
    chmod 777 /data/7za
fi

if [ ! -f "$zipDir"/update ]; then
    rm -rf "$zipDir"/update*
fi

echo "开始解析ROM"
/data/7za x "$zipDir"/"$rn" -r -o"$zipDir"/update >/dev/null
echo "解析完毕"

if [ -f "$zipDir/update/payload.bin" ]; then
    echo "ROM核心文件校验成功"
    source "$zipDir"/update/payload_properties.txt
    update_engine_client --payload=file://"$zipDir"/update/payload.bin --update --headers="
FILE_HASH=$FILE_HASH
FILE_SIZE=$FILE_SIZE
METADATA_HASH=$METADATA_HASH
METADATA_SIZE=$METADATA_SIZE"
    logcat -s update_engine:v
else
    echo "ROM核心文件校验失败，请检查ROM完整性！"
fi