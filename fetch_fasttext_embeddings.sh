
#!/bin/bash
# Aligned Multilingual word Embeddings (jeweils 600MB)
# https://github.com/facebookresearch/MUSE

set -x
set -e

mkdir fasttext
cd fasttext

wget https://dl.fbaipublicfiles.com/arrival/vectors/wiki.multi.de.vec

wget https://dl.fbaipublicfiles.com/arrival/vectors/wiki.multi.en.vec

wget https://dl.fbaipublicfiles.com/arrival/vectors/wiki.multi.hr.vec

wget https://dl.fbaipublicfiles.com/arrival/vectors/wiki.multi.pt.vec

cd ..




#!/bin/bash
# Multi-Modal Word Embeddings (jeweils 4GB)
# https://github.com/VisionLearningGroup/MULE

#set -x
#set -e

#mkdir fasttext
#cd fasttext

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.vec.gz
#gunzip cc.en.300.vec.gz

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.de.300.vec.gz
#gunzip cc.de.300.vec.gz

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.fr.300.vec.gz
#gunzip cc.fr.300.vec.gz

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.cs.300.vec.gz
#gunzip cc.cs.300.vec.gz

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.zh.300.vec.gz
#gunzip cc.zh.300.vec.gz

#wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.ja.300.vec.gz
#gunzip cc.ja.300.vec.gz

cd ..
