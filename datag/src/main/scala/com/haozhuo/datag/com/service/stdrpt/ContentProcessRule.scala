package com.haozhuo.datag.com.service.stdrpt

import com.alibaba.fastjson.serializer.SerializerFeature
import com.alibaba.fastjson.{JSON, JSONObject}
import com.hankcs.hanlp.seg.common.Term
import com.hankcs.hanlp.tokenizer.NLPTokenizer
import com.haozhuo.datag.service.RptStdService

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


/**
 * 小结异常标准化
 * copy Python
 */

object ContentProcessRule {

	val quotation_pattern ="^\"[^\"]*\"$".r
	val normal_scope_pattern ="""([(（](参考值|正常范围|正常值|理想指数).*?[)）])""".r
	val regex_replace_symbol =  """ {1,}([!"#$%&\'()*+,-./:;<=>?@\[\]^_`{|}~\u3002\uff1b\uff0c\uff1a\u201c\u201d\uff08\uff09\u3001\uff1f\u300a\u300b])""".r
	//保健建议模式
	val meaningless_health_suggestion_pattern_1 = """保健建议[:：].*?\n""".r
	val meaningless_health_suggestion_pattern_2 = """保健建议[:：].*?$""".r
	// 标点符号开头的句式
	val symbol_start_sentence_pattern = """^[!"#$%&\'\\*+,.:;=>@\]^_`|}~※\u3002\uff1f\uff01\uff0c\u3001\uff1b\uff1a\u201c\u201d\u2018\u2019\uff09\u300b\u3009\u3011\u300e\u300f\u300c\u300d\ufe43\ufe44\u3014\u3015\u2026\u2014\uff5e\ufe4f\uffe5]+""".r
	// 标点符号结尾的句式
	val symbol_end_sentence_pattern = """[!"#$%&\'\\*,.:;=<@\[^_`|{~\u3002\uff1f\uff01\uff0c\u3001\uff1b\uff1a\u201c\u201d\u2018\u2019\uff08\u300a\u3008\u3010\u300e\u300f\u300c\u300d\ufe43\ufe44\u3014\u3015\u2026\u2014\uff5e\ufe4f\uffe5]+$""".r

	val error_newline_position_pattern ="""\d\.\n[\u4e00-\u9fa5]+""".r

	val weight_data_pattern = """((水份率|体水份率|腰臀比|搏|臀围\(cm\)|推定骨量|脂肪率\(fat%\)|腰围\(cm\)|臀比|脉搏|肌肉量|体脂肪率\(fat%\)|血压|肉量|体脂肪率|体重指数|围\(cm\)|内脏脂肪等级):(\d+/\d+mmhg|\d+次/分|\d+\.\d+|\d+\.|\d+))""".r

	val has_fujian_and_index_sentence_pattern = """[.。]附见: ?1[.,]""".r

	val has_index_symbol_sentence_pattern = """^([^:]*: ?)?(\d(?:\.\d)?[\.,。 ]\D.*?)(\d(?:\.\d)?[\.,。 ]\D.*?)(\d(?:\.\d)?[\.,。 ]\D.*?)?(\d(?:\.\d)?[\.,。 ]\D.*?)?(\d(?:\.\d)?[\.,。 ]\D.*?)?(\d(?:\.\d)?[\.,。 ]\D.*?)?$""".r

	val has_dunhao_symbol_sentence_pattern = """^([^:]*: ?)?(\d(?:\.\d)?、\D.*?)(\d(?:\.\d)?、\D.*?)(\d(?:\.\d)?、\D.*?)?(\d(?:\.\d)?、\D.*?)?(\d(?:\.\d)?、\D.*?)?(\d(?:\.\d)?、\D.*?)?$""".r

	val symbol_split_pattern = """[,:; \u3002\uff1f\uff01\uff0c\u3001\uff1b\uff1a]""".r

	val split_symbol_patternsArray =Array(",(?![^()]*\\))","。(?![^()]*\\))",";(?![^()]*\\))")

	val colon_split_pattern = """ ^([^,:]+:(([^,:]+,)+))([^,:]+:(([^,:]+,)*)[^,:]+)$ """.r

	val clause_has_index_pattern = "^\\d+[\\.,、: ][a-z\\u4e00-\\u9fa5]*"

	val clause_has_index_replace_pattern = "^\\d+[\\.,、: ]"

    val	clause_has_bracket_index_pattern = "^[(（]\\d[)）]*"

	val symbol_clause_pattern = "^\\[!\"#$%&\\'()*\\+,-./:;<=>?@\\[\\]^_`\\{|\\}~\\u3002\\uff1f\\uff01\\uff0c\\u3001\\uff1b\\uff1a\\u201c\\u201d\\u2018\\u2019\\uff08\\u300a\\u3008\\u3010\\u300e\\u300f\\u300c\\u300d\\ufe43\\ufe44\\u3014\\u3015\\u2026\\u2014\\uff5e\\ufe4f\\uffe5\\]*$"


	val bracket_pattern = """^\([^()]*?\)*$"""
	val area_desc_sentence_pattern = "^\\d+(\\.\\d+)?(cm|mm)?( ?[×x\\*] ?\\d+(\\.\\d+)?)? ?[×x\\*] ?\\d+(\\.\\d+)?(cm|mm)(、\\d+(\\.\\d+)?(cm|mm)? ?[×x\\*] ?\\d+(\\.\\d+)?(cm|mm)).*$"

	val basic_check_item_pattern = "^(\\d+)?[./]?\\d+(mmhg|kg|次/分)?$*"

	val suggestion_sentence_pattern = "^,?(建议:?|适度|随访|请结合|避免|注意|防止|[\\u4e00-\\u9fa5]*进一步|请合理|必要时)[cCbCtT\\u4e00-\\u9fa5]+$*"

	val non_chinese_clause_pattern = "^\\[0-9a-z!\"#$%&\\'()*,-./:;<=>?@\\[\\]^_`\\{|\\}~×\\u3002\\uff1f\\uff01\\uff0c\\u3001\\uff1b\\uff1a\\u201c\\u201d\\u2018\\u2019\\uff08\\u300a\\u3008\\u3010\\u300e\\u300f\\u300c\\u300d\\ufe43\\ufe44\\u3014\\u3015\\u2026\\u2014\\uff5e\\ufe4f\\uffe5\\]+$*"

	val symbol_set_pattern = "^\\[!\"#$%&\\'()\\\\*+,\\-./:;<=>?@\\[\\]^_`\\{|\\}~\\u3002\\uff1f\\uff01\\uff0c\\u3001\\uff1b\\uff1a\\u201c\\u201d\\u2018\\u2019\\uff08\\u300a\\u3008\\u3010\\u300e\\u300f\\u300c\\u300d\\ufe43\\ufe44\\u3014\\u3015\\u2026\\u2014\\uff5e\\ufe4f\\uffe5\\]*$"
	val no_obvious_abnormal_pattern = "^[^:, ]*?未(见|发现)明显异常.*$"

	val abn_tct_value_pattern = "^(a?asc-h|asc-us|hsil|lsil|lsil/asc-h).*$"


	def content_process(text:String): List[Object] ={
		val clauses = get_clauses_from_sentence(text)
		val items = new ListBuffer[JSONObject]
		clauses.foreach(x=>{
//			rets.map(ret=>(ret.get("symptoms"),ret.get("parts")))
//			val item  = new JSONObject();
//			item.put("clause",x)
//			import collection.JavaConverters._
//			item.put("rets",JSON.parseArray(JSON.toJSONString(rets.asJava,SerializerFeature.PrettyFormat)))
			items ++= do_sentence_process(x)
		})
		items.toList.distinct
	}

	/**
	 * """
	 * 处理分句结果，提取异常关键词
	 * :param sentence:
	 * :return:
	 * """
	 *
	 * @param line
	 * @return
	 */

	def do_sentence_process(line:String)={

		//var t2 = Tuple2(List.empty[String],List.empty[String])
		// 1. 预处理
		var sent  = sentence_pre_process(line)

		// 2. 提取异常关键词
		//var abn_tags = List.empty[String]
		//var headers = List.empty[String]
		val data = new ListBuffer[JSONObject]
		sent.foreach(x=>{
			val sentT2 = sentence_post_process(x)
			val header = sentT2._1
			val text = sentT2._2
			if(text.nonEmpty) data += extract_abn_tags_hanlp(text)
		})
		data.toList

	}

	/**
	 * 分句后处理
	 *
	 *     1. 过滤无意义分句
	 *     2. 清除两侧标点
	 *     3. 精简分句内容
	 *
	 * :param sentence:
	 * :return:
	 *
	 * @param sentence
	 * @return
	 */
	def sentence_post_process(sentence:String)= {
		var t = Tuple2("","")
		var sent = sentence.replaceAll(symbol_start_sentence_pattern.toString(),"")
		sent = sent.replaceAll(symbol_end_sentence_pattern.toString(),"")
		var headers = ""
		var prefixs =""
		val header = """(tcd所见|tcd提示|tcd检查|tct|tct检测|(其他|提示)\d|疤痕|眼底照相及amsler):""".r.findAllMatchIn(sent)
		if(header.nonEmpty){
			headers = header.toList(0).group(1)
			sent = sent.replaceAll("(tcd所见|tcd提示|tcd检查|tct|tct检测|(其他|提示)\\d|疤痕|眼底照相及amsler):","")
		}
		var prefix = """(((颈|腰|胸)椎?)[ctl]?\d[/\-.][ctl]?\d(、[ctl]?\d[/\-.][ctl]?\d)*)([\u4e00-\u9fa5]+)$""".r.findAllMatchIn(sent)

		if(prefix.isEmpty) prefix = """(((颈|腰|胸)椎?)[ctl]?\d(、[ctl]?\d)*)([\u4e00-\u9fa5]+)$""".r.findAllMatchIn(sent)
		if (prefix.nonEmpty) {
			val prefix1 = prefix.toList(1)
			sent = "{}-{}".format(prefix1.group(1), prefix1.group(-1))
		}
		prefix = """\(?[\dctls\-/、及]+\)?((颈|腰|胸)?(椎|间)[\u4e00-\u9fa5、]+(\([\u4e00-\u9fa5]+\))?)$""".r.findAllMatchIn(sent)
		if(prefix.nonEmpty) sent = prefix.toList(0).group(1)
		// 移除心电图部位信息
		prefix = """(i+|ⅰ|ⅱ|ⅲ|av[lrf]|v\d|\||、|-){2,}轻?度?(([str]+[\u4e00-\u9fa5]+)+)$""".r.findAllMatchIn(sent)

		if(prefix.isEmpty) prefix = """'(v\d)+轻?度?(([str]+[\u4e00-\u9fa5]+)+)$""".r.findAllMatchIn(sent)
		if(prefix.nonEmpty) sent = prefix.toList(1).group(1)
		if(is_meaningless_sentence(sent)) t = (headers,"")
		else t = (headers,sent)
		t
	}

	def extract_abn_tags(sentence:String)={
		var t = new Tuple3(0, Option[String](""), List(sentence))
		val tagcm = """^([\u4e00-\u9fa5]+)\d+(\.?\d+)?[*x]\d+(\.?\d+)?([cm]+)?$""".r.findAllMatchIn(sentence).toList

		if(sentence.isEmpty) t = (1,None,List.empty)
		else if(sentence.matches("\\d+[./]?\\d+?$*")) t = (1,None,List.empty)
		else if(is_meaningless_sentence(sentence)) t =  (1,None,List.empty)
		else if(sentence.matches(no_obvious_abnormal_pattern)||sentence.contains("未见")) t = (1,None,List.empty)
		else if(sentence.equals("nilm")) t = (1,None,List.empty)
		else if(sentence.matches(abn_tct_value_pattern)) t = (2,None,List(sentence))
		else if(sentence.contains("阳性")){
			if(sentence.contains("hpv")) t = (1,None,List("HPV阳性"))
			else if(sentence.contains("hp")) t = (1,None,List("HP阳性"))
			else t = (1,None,List("(名称未确定)阳性"))
		}
		else if(sentence.contains("阴性")){
			t = (1,None,List.empty)
		}
		else if(sentence.matches("骨(量|质)*")){
			val tag = "^骨[量质][\\u4e00-\\u9fa5]+".r.findAllIn(sentence).toList(0)
			Array("骨量正常","骨量基本正常").exists(x=>x.contains(tag)) match {
				case true =>
					t = (1,None,List.empty)
				case false =>
					t = (1,None,List(tag))
			}
		}
		else if("与.*?相比".r.findAllIn(sentence).nonEmpty) t = (1,None,List.empty)
		else if(sentence.matches("(心|肝|胆|脾|肺|膈|胰|胰头|胰体|肾|左肾|右肾|双肾|前列腺|子宫|子宫残端|左附件|右附件|双附件区?|膀胱|残余宫颈):*")){
			val tag = """:([^:]+)$""".r.findAllMatchIn(sentence).toList(0).group(1)
			//println(tag)
			if(tag.isEmpty) t = (1,None,List.empty)
			else if(Array("无","略").exists(x=>x.equals(tag))) t = (1,None,List.empty)
			else t = (1,None,List(tag))
		}
		else if(sentence.contains("视力")){
			val tag = "(视力欠佳|视力减退|(视力)?低于正常范围|视力差)".r.findAllIn(sentence).toList
			if(tag.nonEmpty) t = (1,None,List(tag(0)))
			else t = (1,None,List.empty)
		}
		else if(sentence.matches("[\\u4e00-\\u9fa5\\?]+:*")){
			if(sentence.matches("[\\u4e00-\\u9fa5]+:$*")) t = (1,None,List.empty)
			else if(sentence.matches("(龋齿|缺齿):*")){
				t = (1,None,List("(龋齿|缺齿):".r.findFirstIn(sentence).get))
			}
			else if(sentence.contains("疤痕")){
				val tag = ":([^:]+)$".r.findAllIn(sentence).toList
				if(Array("无","略").exists(x=>x.equals(tag(0)))) t = (1,None,List.empty)
				else t = (1,None,List("疤痕:{}".format(tag)))
			}
			else if(sentence.matches("([\\u4e00-\\u9fa5]+):\\.?(\\d+)?\\.?(\\d+)?[./\\-]?\\d+?\\.?(\\d+)?(mmhg|kg|次/分|cm)?$*")){
				t = (1,Option("([\\u4e00-\\u9fa5]+):\\.?(\\d+)?\\.?(\\d+)?[./\\-]?\\d+?\\.?(\\d+)?(mmhg|kg|次/分|cm)?$".r.findFirstIn(sentence).get),List.empty)
			}
			else if(sentence.contains("血压:")){
				val tag = "mmhg.*?(舒张压正常高值|收缩压正常高值|收缩压偏高|血压正常高值|高血压可能|高血压史|高血压|家族史|血压偏低).*?$".r.findAllIn(sentence).toList
				if(tag.nonEmpty) t = (1,Some("血压"),List(tag(0)))
				else t = (1,Option("血压"),List.empty)
			}
			else if(sentence.matches("[\\u4e00-\\u9fa5]+:([^:]+):([^:]+)?$*")){
				val tags = sentence.split(":(?![^()]*\\))")
				if(tags.length==3){
					if(tags(2).nonEmpty && !tags(2).matches("(如有|门诊|咨询|治疗)*")) t = (1,None,List(tags(2)))
					else t = (1,None,List(tags(1)))
				}
			}
			else if(sentence.contains("视力欠佳")) t = (1,None,List("视力欠佳"))
			else if(sentence.matches("[^0-9a-z]+$*")){
				val tags = sentence.split(":(?![^()]*\\))")
				val tag = tags.filter(x=>"(如有|门诊|咨询|治疗)".r.findAllIn(x).isEmpty)
				t = (1,None,tag.toList)
			}
			else if("""([\u4e00-\u9fa5]+):([^:]+)$""".r.findAllMatchIn(sentence).nonEmpty){
				val tag = """([\u4e00-\u9fa5]+):([^:]+)$""".r.findAllMatchIn(sentence).toList
				val header = tag.head.group(1)
				val value = tag.head.group(2)
				if(!is_meaningless_sentence(value)) t = (1,Some(header),List.empty)
				else t = (1,None,List.empty)
			}
			else t = (1,None,List.empty)
		}
		else if(sentence.matches("[\\u4e00-\\u9fa5]+$*")) t = (1,None,List(sentence))
		else if(sentence.matches("血压(\\d)?\\(mmhg\\):(\\d+\\.?(\\d+)?)?/(\\d+)?\\.?(\\d+)?$*")||sentence.matches("血压1:.*?血压2:.*?")||sentence.matches("血压\\d:(\\d+\\.?(\\d+)?)?/(\\d+)?\\.?(\\d+)?mmhg$*")) t = (1,None,List(sentence))
		else if(sentence.matches("[^:]+:\\d+([./]\\d+)?$")){
			val tag = """(轻度肥胖|中度肥胖|重度肥胖|腹型肥胖|体重偏轻|体脂率大于正常范围|体脂肪率理想范围上限|体脂肪率理想范围下限|大动脉弹性明显下降|大动脉弹性有所下降|强壮型|微胖型|过瘦型|稍瘦型|肥胖型|超重).*?""".r.findFirstMatchIn(sentence)
			if(tag.nonEmpty) t = (1,None,List(tag.get.group(1)))
			else t = (1,None,List.empty)
		}
		else if(sentence.matches("(内脏脂肪等级|z值|测定值|hp值|骨密度正常t-score|测定值|20分钟的dpm值|脂肪率\\(fat%\\)|hpv-dna检测|标准型体脂肪率|体脂肪率理想范围下限|体脂肪率理想范围上限|脂肪率理想范围上限|脂肪率理想范围下限|体脂肪率|胸围围|胸围|腰围|臀围|腹围|头围|骨密度正常z值|检验值|dob值|基础代谢率|身体水分量tbw|z|正常t-score|基础代谢率bmr|血氧饱和度)*"))
			t = (1,None,List.empty)
		else if(sentence.matches("[^:]+:\\d、*")) {
			val sent = sentence.replaceAll("[^:]+:\\d、","")
			t = (1,None,List(sent))
		}
		else if(sentence.matches("[\\u4e00-\\u9fa5]+疤痕\\((.*?)\\)$*")) t = (1,None,List(sentence))
		else if(sentence.matches("""
				|(分别大小|较大直径|高度|较大长径|较大的|大|内径最宽处|右侧最大|右侧内径最宽处|较大者大小|最大的|右侧厚|右侧最宽处内径|'
			   |右侧大的|右后上|右侧较大者|双层厚度|较大者|最大直径线|最大斜径|左侧直径大小|最大径限|内径减少|右较大|左侧长径|最厚处|'
			   |直径分别|之一大小|分别|左侧最宽处内径|右侧|扩张最宽处内径|右侧长径|最大小|左侧大小|较大直径均|最短径|长度|右侧最宽处|'
			   |内径|所示|厚均|考虑左侧|最大层面|双层厚|左侧左右径|亦均可见直径|空洞大小|左侧较大|左侧范围|最大一枚直径|最大界限|'
			   |两个大小分别|右侧大者|范围|内径宽|深|最大径|心胸比率|最大径线|较宽处|较大者直径|心胸比例|横径|值约|厚径|另大小|'
			   |直径最大|最大一枚大小|长|长径|前后|右侧第|径|分离|大小均|右侧直接大小|直径线|左侧|大最|大者大小|厚|左侧最宽|右侧较大|'
			   |左大小|大小分别|厚度|最大截面|前后径大小|深度|其中左侧大小|较大者长径|最大均|最能大|宽径|最大直径|压缩|直径|右侧大小|'
			   |显示长度|宽|最大值径线|较大|右侧最大长径|左侧最大长径|其一大小|测量直径|最大径大小|左侧最宽处|最大位于左侧大小|右侧左右径|'
			   |右侧最宽|前后径|右侧范围|高径|最大层面大小|上段|左侧最大)?约.*|"""  ) ||
		        sentence.matches("(最大为?|大小为?|大(的|者)|其中最大为|一个).*"))  t = (1,None,List.empty)
		else if(sentence.matches("[\\u4e00-\\u9fa5]+\\([^()]+\\)$.*")){
			val tag = """^([\u4e00-\u9fa5]+)\(.*?\d.*?\)$""".r.findAllMatchIn(sentence).toList
			if(tag.nonEmpty) t = (1,None,List(tag.head.group(1)))
			else t = (1,None,List(sentence))
		}
		else if(tagcm.nonEmpty) t = (1,None,List(tagcm.head.group(1)))
		else if("""(.*?)(约|大小约|最大约|大者约|较大约|范围约)?\d+(\.?\d+)?[*x](\d+(\.?\d+)?[*x])*\d+(\.?\d+)?([cm]+)""".r.findAllMatchIn(sentence).nonEmpty){
			val tag = """(.*?)(约|大小约|最大约|大者约|较大约|范围约)?\d+(\.?\d+)?[*x](\d+(\.?\d+)?[*x])*\d+(\.?\d+)?([cm]+)""".r.findAllMatchIn(sentence).toList
			t = (1,None,List(tag.head.group(1)))
		}else if(sentence.matches("[\\u4e00-\\u9fa5]+[ctl]?\\d[/\\-.][ctl]?\\d(、[ctl]?\\d[/\\-.][ctl]?\\d)*[\\u4e00-\\u9fa5]+.*$")
			|| sentence.matches("[\\u4e00-\\u9fa5]+[ctl]?\\d(、[ctl]?\\d)*[\\u4e00-\\u9fa5]+.*")
		) t = (1,None,List(sentence))


		t
	}

	def extract_abn_tags_hanlp(text:String)={

		val parts = new ListBuffer[String]
		val symptoms = new ListBuffer[String]
		val words = new ListBuffer[String]
		val full_words = new ListBuffer[String]
		NLPTokenizer.segment(text).toArray(Array[Term]()).foreach(x=> {
			val word = x.word
			if(RptStdService.partMap.containsKey(word)) parts += word
			if(RptStdService.symptomsMap.containsKey(word)) symptoms += word
			full_words += x.toString
			words += word
		})
		val rs  = new JSONObject();
		//rs.put("words",words.toList.mkString(","))
		rs.put("symptoms",symptoms.toList.mkString(","))
		rs.put("parts",parts.toList.mkString(","))
		rs
	}




	def is_meaningless_sentence(line:String): Boolean ={
		var bool = false
		if(line.length==1) bool = true
		else if(line.matches(area_desc_sentence_pattern)||line.matches(basic_check_item_pattern)||line.matches("\\d+(:\\d+)+$*")) bool = true
		else if(line.matches(suggestion_sentence_pattern)) bool = true
		else if(line.matches("\\(?大(小|者|的)(直径)?约*") || line.matches("\\(?最(大|小)(经线)?(约|者|范围|长径)*") || line.matches("\\(?最宽处(内径)?均?约*") ) bool = true
		else if(line.matches(non_chinese_clause_pattern)) bool =true
		else if(line.matches("\\(?(请|详见|进一步|补充意见|综合|建议|定期|宜|多(食|吃)|多由于|备注|原因|仅供|参考值|已(与|告知|复测)|正常(值|范围)|注:|替检|加强)*")) bool = true
		else if(line.matches("hr\\d+次/分$*")) bool = true
		else if(line.contains("弃查")) bool = true
		else if(line.matches(symbol_set_pattern)) bool = true
		bool
	}
	/**
	 *
	 */
	def sentence_pre_process(sentence:String): List[String] ={
		var sent = sentence.replaceAll("[·①②③⑵▼\\ue0d3\\ue1bd\\ue2b1\\ue2cd\\ue37f\\ue3c2\\ue4c6\\ue5cc\\ue788\\[\\]【】]","")
		sent = sent.replaceAll("^(tct检测|[^:]+平扫):1 ?","")
		sent = sent.replaceAll("^[^:]+(平扫|mtd|mri|mra)(示|见)?:?1?\\.?","")
		sent = sent.replaceAll("(备注)?\\.?:?\\(?建议.*?$","")

		// 去掉两侧的括号
		if(sent.matches(bracket_pattern)){
			sent = sent.substring(1,sent.length-1)
		}
		sent = sent.replaceAll(symbol_start_sentence_pattern.toString(),"")
		var clause = List.empty[String]
		if(sent.contains(" ")){
			sent = sent.replaceAll("(心|肝|胆|脾|肺|膈|胰|胰头|胰体|左肾|右肾|双肾|前列腺|子宫|子宫残端|左附件|右附件|双附件区?|膀胱|残余宫颈) ","\\1、")
			sent = sent.replaceAll("([,、 ])(心|肝|胆|脾|肺|膈|胰|胰头|胰体|左肾|右肾|双肾|前列腺|子宫|子宫残端|左附件|右附件|双附件区?|膀胱|残余宫颈) (心|肝|胆|脾|肺|膈|胰|胰头|胰体|左肾|右肾|双肾|前列腺|子宫|子宫残端|左附件|右附件|双附件区?|膀胱|残余宫颈)","\\1\\2、\\3")

			//   龋齿
			if(sent.matches("(龋齿|缺齿|牙齿|其他)*")){
				sent = sent.replaceAll("([上下左右]+) (\\d)","\\1\\2")
				sent = sent.replaceAll(" ([上下左右]+\\d)","\\1")
				sent = sent.replaceAll(" (\\d+)","、\\1")
				sent = sent.replaceAll(",(\\d)","、\\1")
			}
			//按照空格分割
			clause = sent.split(" (?![^()]*\\))").toList
		}
		if(sent.contains(",")){
			clause = sent.split(",(?![^()]*\\))").toList
		}
		if(sent.matches("-[\\u4e00-\\u9fa5]*")){
			sent = sent.replaceAll("^-([\\u4e00-\\u9fa5])","$1")
		}
		if(clause.nonEmpty){
			clause = clause.filter(x=> x.trim.nonEmpty).map(x=>x.trim)
		}else{
			clause = List(sent)
		}
		clause
	}





	/**
	 *
	 * @param sentence
	 */
	private def  get_clauses_from_sentence(sentence:String): List[String] ={
		var result = List.empty[String]
		var sent:String =  sentence.trim().toLowerCase
		sent = ToDBC(sent)

	/*	quotation_pattern.findFirstIn(sent)match {
			case Some(data) =>
				sent = sent.replace("\"","")
			case _ =>
		}
		println(sent)
		sent = sent.replace("\\r", "\r").replace("\\n", "\n")

		//sent = sent.replaceAll(regex_replace_symbol,)
		sent = findFirstByReg(normal_scope_pattern,sent)
		sent = findFirstByReg(regex_replace_symbol,sent)*/
		//sent = sent.replaceAll("\\s+", " ")
		sent = remove_extra_spaces_or_symbols(sent)
		//println(sent)
		sent = remove_suggestion_content(sent)
		sent = remove_start_end_symbols(sent)

		sent = remove_error_newline_symbols(sent)

		if(sent.matches("\\d+\\.?\\d+?$*")){
			result
		}else if(sent.contains("上岗类别")&&sent.contains("起止时间")&&sent.contains("工作单位")){
			result
		}else if(sent.matches("^((水份率|体水份率|腰臀比|搏|臀围\\(cm\\)|推定骨量|脂肪率\\(fat%\\)|腰围\\(cm\\)|臀比|脉搏|肌肉量|体脂肪率\\(fat%\\)|血压|肉量|体脂肪率|体重指数|围\\(cm\\)|内脏脂肪等级):(\\d+/\\d+mmhg|\\d+次/分|\\d+\\.\\d+|\\d+\\.|\\d+) ?)+$*")){
			val clauses = weight_data_pattern.findAllIn(sent)
			if (clauses.nonEmpty) result = clauses.toList
		}else{
			var new_sub_sentences = List.empty[String]
			val sub_sentences = split_sentence_has_newline_symbols(sent)

			sub_sentences foreach(x=>{
				var sub:String = x
				if(x.startsWith("\n")) sub =x.replaceAll("\n","")
				has_fujian_and_index_sentence_pattern.findFirstIn(sent) match {
					case Some(xs)=>
						val rets =  xs.split("附见:")
						rets.foreach(f=>{
							new_sub_sentences = new_sub_sentences :+ split_sentence_has_clause_index(f)
						})
					case _ =>
						new_sub_sentences = new_sub_sentences :+ split_sentence_has_clause_index(x)
				}

			})

			var clauses_n = List.empty[String]


			new_sub_sentences.foreach(x =>{
				val clause_set = split_sentence_has_clause_symbols(x)
				clause_set.filter(x=> !x.matches("\\d+\\.?\\d+?$*")).foreach(
					x=>{
						if(x.length>20 && x.contains(",")){
							clauses_n = clauses_n ++ x.split(split_symbol_patternsArray(0)).toList
						}else{
							clauses_n = clauses_n :+ x
						}
					}
				)
			})
			result = do_clauses_clean(clauses_n)


		}

		result
	}

	/**
	 * """
	 * 完成分句清理
	 *     1. 清理分句句首的序号
	 *     2. 清理分句句首和句末的标点
	 *     3. 清理无意义分句
	 *
	 * :param clauses:
	 * :return:
	 * """
	 *
	 * @param clauses
	 * @return
	 */
	def do_clauses_clean(clauses:List[String]):List[String] = {

		var new_clauses = List.empty[String]
		clauses.foreach(x=>{
			val trx = x.trim
			var clause = trx
			if(trx.matches(clause_has_index_pattern)){
				clause = trx.replaceAll(clause_has_index_replace_pattern,"")
			}
			if(trx.matches(clause_has_bracket_index_pattern)){
				clause = trx.substring(3,trx.length)
			}
			clause = remove_start_end_symbols(clause)
			if(clauses.nonEmpty && !clause.matches(symbol_clause_pattern)){
				new_clauses = new_clauses :+ clause
			}

		})

		new_clauses
	}


	/**
	 *   按照标点符号分割
	 * @param symbol
	 * @param sentence
	 * @return
	 */
	def split_sentence_by_symbol(symbol:String, sentence:String): List[String]={
		var list:List[String] = List()
		if(sentence.length>20 && symbol.equals(",")){
			list = sentence.split(split_symbol_patternsArray(0)).toList
		}else if(symbol.equals("。")){
			sentence.split(split_symbol_patternsArray(0)).toList
		}else if(symbol.equals(";")){
			sentence.split(split_symbol_patternsArray(0)).toList
		}else{
			list = sentence+:list
		}

		list
	}

	/**
	 * 统计数组字符出现的次数
	 */
	def countWords(str: List[String],symbol:String): Int = {
		var num = 0

		str.foreach(x=>{
			if(x.endsWith(symbol)) //判断最后一个字符是不是0,不是0:需要总数减1,是0：不需要改变
				num = num + x.split(symbol).length
			else
				num = num +	x.split(symbol).length-1
		})
		num
	}



	/**
	 *  根据提供的分句符号分割句子
	 * @param sentence
	 * @return
	 */

	def split_sentence_has_clause_symbols(sentence:String):List[String] = {
		var b = false
		var clause = List.empty[String]
		var sent:String = sentence
		//移除句首句末的标点
		sent = remove_start_end_symbols(sent)
		sent = remove_extra_spaces_or_symbols(sent)
		//判断是否包含标点符号， 不识别括号内的标点符号
		val sent_ =  sent.replaceAll("\\(.*?\\)","")

		val symbols = symbol_split_pattern.findAllIn(sent_).toList
		val symbols_count = symbols.size
		val distinct_symbols = symbols.distinct
		val distinct_symbols_count = distinct_symbols.size

		// 1. 无标点
		if(symbols.isEmpty){
			clause = clause :+ sent
		}else if(symbols_count==1){
			val symbol = symbols(0)
			List("、",":"," ").exists(x=>symbol.equals(x))match {
				case true =>
					clause = sent +: clause
				case false =>
					clause = split_sentence_by_symbol(symbol,sent)
			}
		} // 3. 有多个相同标点
		else if(distinct_symbols_count == 1){
			val symbol = distinct_symbols(0)
			if (List("、", ":", " ").exists( symbol equals _)) clause = sent +: clause
			else clause = split_sentence_by_symbol(symbol, sent)
		}else if(distinct_symbols_count==2){
			if(distinct_symbols.equals(Array(":",","))&&countWords(symbols,":")>=2){
				val charList = colon_split_pattern.findAllIn(sent).toList.head.toList
				if(charList.nonEmpty){
					charList.foreach(
						x=>{
							if (charList.indexOf(x)% 3 ==0) clause = clause :+ x.toString
						}
					)
				}else{
					b = true
				}
			}else{
				b = true
			}
		}
		if(b){
			if(distinct_symbols.contains("。")){
				val sub_sentences =	sent.split(split_symbol_patternsArray(1))
				if(distinct_symbols.contains(";")){
					sub_sentences.foreach(x=>{
						clause = clause ++ x.split(split_symbol_patternsArray(2))
					})
				}else{
					clause = clause ++ sub_sentences
				}
			}else if(distinct_symbols.contains(";")){
				clause = sent.split(split_symbol_patternsArray(2)).toList
			}else{
				clause = clause :+ sent
			}
		}else{
			clause = clause :+ sent
		}
		clause
	}

	/**
	 * 含有分句序号的句子分割
	 * @param sentence
	 */
	def split_sentence_has_clause_index(sentence:String)={
		var sent:String = sentence
		val hs = has_index_symbol_sentence_pattern.findAllIn(sentence)
		if(hs.nonEmpty) sent = hs.mkString(",").split(",")(0)
		val sub_sentences = has_dunhao_symbol_sentence_pattern.findAllIn(sentence)
		if(sub_sentences.nonEmpty) sent = sub_sentences.mkString(",").split(",")(0)
		sent
	}

	def split_sentence_has_newline_symbols(sentence:String): Array[String]={
		if(sentence.contains("\r")||sentence.contains("\n")){
			sentence.split("(\\r\\n|\\r|\\n)").filter(x=> x.trim.nonEmpty )
		}else{
		  Array(sentence)
		}
	}

	/**
	 * 移除位置异常的换行符
	 * 如：
	 *  1.\n两肺CT平扫未见明显异常。2.肝脏及右侧肾脏点状致密影，请结合其他检查。
	 *  1.两肺上叶及右肺中叶多发磨玻璃结节灶，建议抗炎治疗后复查。\n2.两肺少许纤维灶。\n3.右侧斜裂胸膜局部轻度增厚。\n4.\n胸主动脉管壁局部钙化。
	 * @param sentence
	 * @return
	 */

	def remove_error_newline_symbols(sentence:String):String={
		var sent = sentence
		error_newline_position_pattern.findFirstIn(sent)match {
			case Some(x) =>
				sent = sent.replace("\n","")
			case _ => None
		}
		sent
	}

	/**
	 * 移除句首句末的标点
	 * @param sentence
	 * @return
	 */

	def remove_start_end_symbols(sentence:String):String={
		var sent = sentence
		// 移除句首标点
		sent = sent.replaceAll(symbol_start_sentence_pattern.toString(),"")
		// 移除句末标点
		sent = sent.replaceAll(symbol_end_sentence_pattern.toString(),"")
		sent

	}

	/**
	 * 移除多余的空格或标点
	 * @param sentence
	 */

	def remove_extra_spaces_or_symbols(sentence:String): String ={

		var sent = sentence

		quotation_pattern.findFirstIn(sent)match {
			case Some(data) =>
				sent = sent.replace("\"","")
			case _ =>
		}

		sent = sent.replace("\\r", "\r")
		sent = sent.replace("\\n", "\n")
		sent = sent.replace("\t","")
		//sent = sent.replaceAll("\\s+", " ")
		sent=sent.replaceAll("\\n{2,}","\n").replaceAll("([.\\-?]){2,}","$1")
		sent = sent.replaceAll("[\\ue0d3\\ue1bd\\ue2b1\\ue2cd\\ue37f\\ue3c2\\ue4c6\\ue5cc\\ue788]+","?")
		sent = remove_extra_space(sentence)
		sent = replace_error_symbols(sentence)
		//println(sent)

		sent = sent.replaceAll("[\\ue0d3\\ue1bd\\ue2b1\\ue2cd\\ue37f\\ue3c2\\ue4c6\\ue5cc\\ue788]+","?")

		sent
	}

	/**
	 * 对错误使用的标点进行替换
	 *
	 * :param sentence:
	 * :return:
	 *
	 * @param sentence
	 */
	def replace_error_symbols(sentence:String): String ={

		var sent = sentence
		sent = sent.replace("\\r", "\r")
		sent = sent.replace("\\n", "\n")
		sent = sent.replace("\t","")
		// 3-4；4-5；5-6椎间孔狭窄\n颈椎钩突关节增生\n颈椎项韧带部分钙化｜将\d-\d后面的分号;替换为顿号
		// 腰2-3,3-4,4-5及腰5-骶1椎间盘轻度膨隆。｜将\d-\d后面的逗号,替换为顿号
		// 1，腰椎骨质增生\n2，第2/3,3/4,4/5,5/骶1椎间盘突出？建议CT\n3第3,4椎体滑脱｜将\d/\d后面的逗号,替换为顿号
		sent =  sent.replaceAll("(\\d[-/]\\d)[;,]","$1")
		//体脂肪率:28.8\n体脂肪率(FAT%)\n体脂率测定正常范围\n:28.8｜将冒号前的换行\r\n替换为空
		sent =	sent.replaceAll("(\\r|\\n)+:",":")
		// 体重指数:18.10\n血压:\n101/57mmHg\n收缩压:\n101\n舒张压:57｜将冒号后的换行\r\n替换为空
		sent = sent.replaceAll(":(\\r|\\n)+",":")
		// 体重指数:26.00\n血压:147/93mmHg\n双眼裸视力;1.0｜将视力后面的分号替换为冒号
		sent = sent.replaceAll("(视力);(\\d)","$1:$2")
		// 其他检查:胸膝位：距肛缘0。5*1。0cm疑似大小新生物（突起）｜将相乘的数字中的中文。替换为 .
		sent = sent.replaceAll("(\\*\\d+)。(\\d+)","$1.$2")
		sent = sent.replaceAll("(\\d+)。(\\d+\\*\\d+)","$1.$2")
		sent
	}


	def findFirstByReg(regex:Regex,content:String): String ={
		var result:String = content
		regex.findFirstIn(content) match {
			case Some(data) =>
				result = result.replaceAll(regex toString(),"")
			case _=>
		}
		result
	}

	/**
	 * 理建议内容
	 * @param sentence
	 */
	def remove_suggestion_content(sentence:String): String ={

		// 1. 清除包含多条建议内容的建议数据，如
		// 双侧乳腺小叶增生:\n建议：1）多吃豆类，果蔬菜，少吃含脂肪类的食品。2）保持心情舒畅，多运动，防止肥胖。3）禁止滥用避孕药和富含雌性激素的保健食品。4）定期乳腺彩超检查，必要时专科诊治。\n\n
		// 一.双侧乳腺增生。与内分泌紊乱有关。建议：1.保持心情舒畅，生活规律；2.佩戴合适疏松乳罩；3.定期行红外乳透或乳腺彩超检查，必要时做钼靶检查；4.专科治疗；5 .糖类抗原CA15-3定量筛查。 \n二.双侧乳腺包块待查，建议进一步检查。
		var sent = sentence
		val reg = """[^(保健)]建议:1""".r
		reg.findFirstIn(sentence) match {
			case Some(data) =>
				"""二[\.、]""".r.findFirstIn(sent)match {
					case Some(x)=>
						sent = sent.replaceAll("(([^(保健)])建议:1.*?)(二[\\.、])","\2\3")
					case _=>
						sent = sent.replaceAll("[^(保健)]建议:1.*$","")
				}
			case _ => None

		}
		meaningless_health_suggestion_pattern_1.findFirstIn(sent)match {
			case Some(data)=>
				sent = sent.replaceAll(meaningless_health_suggestion_pattern_1.toString(),"")
				meaningless_health_suggestion_pattern_2.findFirstIn(sent)match {
					case Some(x)=>
						sent = sent.replaceAll(meaningless_health_suggestion_pattern_2.toString(),"")
				}
			case _=>None
		}

		// 3. 移除其他符合清除模式的建议内容
		sent = sent.replaceAll(",?(建议:?|适度|随访|请结合|避免|注意|防止|[\\u4e00-\\u9fa5]*进一步|请合理|必要时)[cCbCtT\\u4e00-\\u9fa5]+[.，！]","")

		// 需要保存末尾标点
		sent = sent.replaceAll(",?(建议:?|适度|随访|请结合|避免|注意|防止|[\\u4e00-\\u9fa5]*进一步|请合理|必要时)[cCbCtT\\u4e00-\\u9fa5]+([ ,。；\\n\\r])","")
		// 一直匹配至句末

		sent = sent.replaceAll(",?(建议:?|适度|随访|请结合|避免|注意|防止|[\\u4e00-\\u9fa5]*进一步|请合理|必要时)[cCbCtT\\u4e00-\\u9fa5]+$","")
		sent
	}

	def remove_extra_space(sentence:String): String ={
		// 名称中包含空格


		var sent:String = sentence
		sent = sent.replace("\\r", "\r")
		sent = sent.replace("\\n", "\n")
		sent = sent.replace("\t","")

		sent = sent.replaceAll("综 ?合 ?征","综合征")
		sent = sent.replaceAll("炎 症","炎症")
		sent = sent.replaceAll("肺 ?结 ?核","肺结核")
		sent = sent.replaceAll("右 ?肺 ?上叶","右肺上叶")
		sent = sent.replaceAll("牙 ?结 ?石","牙结石")
		sent = sent.replaceAll("详 ?见 ?纸 ?质 ?报 ?告","详见纸质报告")
		sent = sent.replaceAll("(脂肪肝) (轻|中|重)","$1$2")
		sent = sent.replaceAll("眼 底","眼底")
		sent = sent.replaceAll("未 见异常","未见异常")
		sent = sent.replaceAll("颈椎 病","颈椎病")
		sent = sent.replaceAll("胆囊 结石","胆囊结石")
		sent = sent.replaceAll("窦 炎","窦炎")
		sent = sent.replaceAll("非典型预激lg l","非典型预激lgl")
		sent = sent.replaceAll("(孕\\d+) (周)","$1$2")
		sent = sent.replaceAll("(腰|颈) ?(\\d+) ?(椎)","$1$2$3")


		// 前后或中间不适合出现空格的位置出现空格，去掉空格
		sent = sent.replaceAll(" (t-score)","$1")
		sent = sent.replaceAll("(c-fpwv) (\\d)","$1:$2")
		sent = sent.replaceAll("(hr) (\\d)","$1$2")
		sent = sent.replaceAll("(hpv|dna) ","$1")
		sent = sent.replaceAll(" (均|阴|阳)","$1")
		sent = sent.replaceAll("(hp) ((阳|阴))","$1$2")
		sent = sent.replaceAll("((阳|阴)性) (dpm)","$1$3")
		sent = sent.replaceAll("(参考值) (dpm)","$1$2")
		sent = sent.replaceAll("(和|为|是)(:)? ","$1$2")
		sent = sent.replaceAll("(i+|ⅰ|ⅱ|ⅲ|av[lrf]|t)+ (v\\d)","$1$2")
		sent = sent.replaceAll("([tr]波(略|置|稍|向|推)?|低平|(双向)?倒置(向|略)?|双向|st段|轻度?) ","$1")
		sent = sent.replaceAll(" (r/s)","$1")
		sent = sent.replaceAll("(av[lf]) (q)","$1-$2")
		sent = sent.replaceAll(" ?(呈) ?(q[sr])","$1$2")
		sent = sent.replaceAll("(呈q) ([sr])","$1$2")
		sent = sent.replaceAll("(呈(q[sr])) (型)","$1$3")
		sent = sent.replaceAll("(av[rlf]) (q[rs]) (型)","$1呈$2$3")
		sent = sent.replaceAll("(t|r|q|p) (波)","$1$2")
		sent = sent.replaceAll("(v\\d) ([rstlf])","$1$2")
		sent = sent.replaceAll("((i+|ⅰ|ⅱ|ⅲ|av[lrf])+) ","$1")
		sent = sent.replaceAll("(v\\d) ","$1")
		sent = sent.replaceAll("(t|r|q) (v)","$1$2")
		sent = sent.replaceAll(" ([>=<≥])","$1")
		sent = sent.replaceAll("(\\^\\d+) ","$1")
		sent = sent.replaceAll("(st) (下移)","$1$2")
		sent = sent.replaceAll("([左右上下]) ([左右上下]) ?","$1$2")
		sent = sent.replaceAll("(c\\d+) (hp.\\d+(.\\d+)) (阴|阳)","")
		sent = sent.replaceAll("(腰|颈) (椎)","$1$2")
		sent = sent.replaceAll("(第一) (三)","$1$2")
		sent = sent.replaceAll("(腰|颈) ?(椎)? (\\d|第) ?","$1$2$3")
		sent = sent.replaceAll(" ?(ct) ?(平扫)","$1$2")
		sent = sent.replaceAll(" ?([tb]i) —?(rads) ?","$1-$2")
		sent = sent.replaceAll("(rads) ","$1")
		sent = sent.replaceAll(" ?([高轻中重]度) ","$1")
		sent = sent.replaceAll(" (可能)","$1")
		sent = sent.replaceAll(" ?([iⅱⅲⅳab]+) ?(级)","$1$2")
		sent = sent.replaceAll("(\\d-\\d) (后纵|水平|椎间盘|间盘)","$1$2")
		sent = sent.replaceAll("([cl]\\d[-/]\\d) ","$1")
		sent = sent.replaceAll(" ?(第) (-?\\d+) ","$1$2")
		sent = sent.replaceAll("(第) ([\\d一二三四五六七八九])","$1$2")
		sent = sent.replaceAll(" ?(\\d+) (项|次)","$1$2")
		sent = sent.replaceAll("(疑似|约|提示|[双左右]侧|相比|双) ","$1")
		sent = sent.replaceAll(" (影|结石|内钙化|陈旧灶)","$1")
		sent = sent.replaceAll(" (回声) ","$1")
		sent = sent.replaceAll(" (疑似,)","$1")
		sent = sent.replaceAll(" (疑似)$","$1")
		sent = sent.replaceAll(" (iu/mm?l|hu|次/分)","$1")





		// 糜烂 ⅱ 度
		sent = sent.replaceAll(" ?(i+|ⅱ|ⅲ|ⅳ|ⅰ|1|2|3) ?(度)","$1$2")
		// 其他
		sent = sent.replaceAll("性 c=","性c=")
		sent = sent.replaceAll("与 同龄","与同龄")
		sent = sent.replaceAll("结合 ","结合")
		sent = sent.replaceAll("建议 ","建议")
		sent = sent.replaceAll(" 拟bi","拟bi")
		sent = sent.replaceAll("avf ","avf-")


		// 带空格的大小描述，去掉空格
		sent = sent.replaceAll(" ?(\\d+(\\.\\d+)?) ?(mm|cm)","$1$3")
		sent = sent.replaceAll(" ?(\\d+(\\.\\d+)?)(mm|cm)? ?(\\*|x|×) ?(\\d+(\\.\\d+)?)(mm|cm)?","$1$3$4$5$7")
		// 带空格的病变级别描述，去掉空格
		sent = sent.replaceAll("' ?(\\d(-?[\\da-z])?) ?(级|类)","$1$3")

		// 前后有空格的标点，去掉空格
		sent = sent.replaceAll(" {1,}([!\"#$%&\\')*+,-./:;<=>?@\\[\\]^_`{|}~\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b])","$1")
		sent = sent.replaceAll("([!\"#$%&\\'(*+,-./:;<=>@\\[\\]^_`{|}~\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b]) {1,}","$1")
		// 年/月前的数字，去掉两侧空格
		//sent = sent.replaceAll(" ?(\\d) (年|月)","\1\2")
		sent
	}

	def ToDBC(input: String): String = {
		val c = input.toCharArray
		for (i <- 0 until c.length) {
			if (c(i) == '\u3000') c(i) = ' '
			else if (c(i) > '\uFF00' && c(i) < '\uFF5F') c(i) = (c(i) - 65248).toChar
		}
		val returnString = new String(c)
		returnString
	}

	def main(args: Array[String]): Unit = {
//		val text = content_process("甲状腺右叶内见多个无回声区，最大约0.7*0.5*0.7cm，左叶内见多个低回声区，最大约0.5*0.2*0.5cm，峡部未见明显异常。")
//		val pattern = "(S|s)cala".r
//		val str = "Scala is scalable and cool"
//		var phone = "18310005432"
//		phone = phone.replaceAll("(\\d{3})\\d+(\\d{4})", "$1$2")
//		System.out.println(phone) // 183****543
//		//println(pattern replaceSomeIn (str, "Java"))
//		println("骨头".matches("骨(量|质)$*"))
		//extract_abn_tags_hanlp("子宫:子宫肌瘤\\r\\n   宫颈囊肿\\r\\n   左附件、右附件未发现明显异常\\r\\n\\r\\n")
		val rs = content_process("子宫:子宫肌瘤\\r\\n   宫颈囊肿\\r\\n   左附件、右附件未发现明显异常\\r\\n\\r\\n")
		//rs.foreach(x=>println(x._2))
		import collection.JavaConverters._
		println(JSON.toJSONString(rs.asJava,SerializerFeature.BeanToArray))
		println(rs.toString())
	}

}
