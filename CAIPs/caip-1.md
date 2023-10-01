---
المقابلة: 1
العنوان: الغرض والمبادئ التوجيهية من أجل مكافحة الفساد
الحالة: الاستعراض
النوع: meta
         صاحب البلاغ: ليجي           <ligi@ligi.de>
أنشئت: 2019-08-31
تحديث: 2019-08-31
---

##           ما هو الـ (كايب) ؟         

ويقف فريق الخبراء عن اقتراح تحسين نظام التسلسل الأغاني. () الوثيقة الخاصة بالتصميم التي تقدم معلومات إلى المجتمع المحلي أو تصف معياراً يمكن استخدامه في مختلف سلاسل متعددة. ولكي يكون هذا أكثر دقة، يمكن أن يصف البرنامج القدرات المنطبقة على أي أوامر متتالية للالتزامات المتعجلة بشكل متقطع، بما في ذلك نظم السلاسل غير المجهزة التي يمكن أن تتفاعل مع سلاسل كتلة الإنتاج، مثل نظام المساعدة الإنمائية، والنظم المظلمة، ونظم الهدية التي تستخدم PKI القائمة على أساس الأسير، وما إلى ذلك. وينبغي أن يوفر فريق الخبراء الاستشاري تكنولوجيا موجزة.(ب) تحديد خاصية و أساس منطقي لها ويتولى صاحب البلاغ المعني بالبناء في مجال بناء توافق في الآراء داخل المجتمع المحلي وتوثيق الآراء المخالفة.

## CAIP Rationale

وفي الوقت الراهن، كثيرا ما يُستخدم أيضاً في سلسلة أخرى، مثل استخدام BIP39 في تطبيقات الإيثروم. كما أنه لا يوجد مكان حقيقي لاقتراح معيار يمكن استخدامه في سلاسل متعددة (مثل الأمونيونيات) حالياً. والقصد من هذه الثغرة هو أن تكون مكاناً يمكن أن تعيش فيه هذه المعايير..

## أشكال ونماذج CAIP

         وينبغي أن تُكتب في هذه البرامج      [ألف - علامة][]     الشكل.        
    الأصولالأصول`     () ملف هذا البرنامج على النحو التالي:     `الأصول/caip-n`     (أين)     **نون**     ومن المقرر الاستعاضة عن رقم البرنامج. عندما يرتبط بصورة في جهاز الاتصال المركزي، يستخدم روابط نسبية مثل     `./الأصول/caip-1/الصورة.png`.

##     ديباجة كايب هيدر   

   يجب أن يبدأ كل من هذه البرامج     [(ف ف ف ج) 822](https://www.ietf.org/rfc/rfc822.txt)     ديباجة رأس الأسلوب، سبقها وتلها ثلاثة مبالغ)   `---`   هذا العنوان يُطلق عليه أيضاً     ["المادة الأولى" من قبل (جيكل)](https://jekyllrb.com/docs/front-matter/)ويجب أن يظهر العناوين حسب الترتيب التالي. وتتسم هذه العناية مع " "" اختيارية ويرد وصفها أدناه. جميع العناوين الأخرى مطلوبة.

` caip:` <CAIP number> (this is determined by the CAIP editor)

` title:` <CAIP title>

` author:` <a list of the author's or authors' name(s) and/or username(s), or name(s) and email(s). Details are below.>

` * discussions-to:` \<a URL pointing to the official discussion thread\>

` status:` <Draft | Rejected | Review | Last Call | Withdrawn | Final | Superseded>

`* review-period-end:` <date review period ends>

` type:` <Standard | Informational | Meta>

` * category:` <Core | Networking | Interface | ERC>

` created:` <date created on>

` * updated:` <comma separated list of dates>

` * requires:` <CAIP number(s); if multiple, use `[1,2]` format to create a YAML array>

` * replaces:` <CAIP number(s); if multiple, use `[1,2]` format to create a YAML array>

` * superseded-by:` <CAIP number(s) | URL of non-CAIP standard >

Headers that permit lists must separate elements with commas.

Headers requiring dates will always do so in the format of ISO 8601 (yyyy-mm-dd).

#### `author` header

The `author` header optionally lists the names, email addresses or usernames of the authors/owners of the CAIP. Those who prefer anonymity may use a username only, or a first name and a username. The format of the author header value must be:

> Random J. User &lt;address@dom.ain&gt;

or

> Random J. User (@username)

if the email address or GitHub username is included, and

> Random J. User

if the email address is not given.

#### `resolution` header

#### `discussions-to` header

While a CAIP is a draft, a `discussions-to` header will indicate the mailing list or URL where the CAIP is being discussed.

As a single exception, `discussions-to` cannot point to GitHub pull requests.

#### `type` header

The `type` header specifies the type of CAIP: Standard, Meta, or Informational.

#### `created` header

The `created` header records the date that the CAIP was assigned a number. Both headers should be in yyyy-mm-dd format, e.g. 2001-08-14.

#### `updated` header

The `updated` header records the date(s) when the CAIP was updated with "substantial" changes. This header is only valid for CAIPs of Draft and Active status.

#### `requires` header

CAIPs may have a `requires` header, indicating the CAIP(s) on which this CAIP depends. Note that if the CAIP requires multiple others, the value should be an array of integers (no `"` needed) and/or URLs (wrapped in `"`s) within square brackets (`[]`).

#### `superseded-by` and `replaces` headers

CAIPs may also have a `superseded-by` header indicating that a CAIP has been rendered obsolete by a later document; the value is the number of the CAIP that replaces the current document. The newer CAIP must have a `replaces` header containing the number of the CAIP that it rendered obsolete.

## Auxiliary Files

CAIPs may include auxiliary files such as diagrams. Such files must be named CAIP-XXXX-Y.ext, where “XXXX” is the CAIP number, “Y” is a serial number (starting at 1), and “ext” is replaced by the actual file extension (e.g. “png”).

## Transferring CAIP Ownership

It occasionally becomes necessary to transfer ownership of CAIPs to a new champion. In general, we'd like to retain the original author as a co-author of the transferred CAIP, but that's really up to the original author. A good reason to transfer ownership is because the original author no longer has the time or interest in updating it or following through with the CAIP process, or has fallen off the face of the 'net (i.e. is unreachable or isn't responding to email). A bad reason to transfer ownership is because you don't agree with the direction of the CAIP. We try to build consensus around a CAIP, but if that's not possible, you can always submit a competing CAIP.

If you are interested in assuming ownership of a CAIP, send a message asking to take over, addressed to both the original author and the CAIP editor. If the original author doesn't respond to email in a timely manner, the CAIP editor will make a unilateral decision (it's not like such decisions can't be reversed :)).

## CAIP Editors

Editorial duties to update and maintain the CAIPs is the primary duty of the chair of the editorial working group at CASA. For current working group chair, see [CASA's administrative homepage](https://github.com/chainagnostic/casa#working-groups).

## CAIP Editorial Process

For each new CAIP that comes in, an editor does the following:

- Read the CAIP to check if it is ready: sound and complete. The ideas must make technical sense, even if they don't seem likely to get to final status.
- The title should accurately describe the content.
- Check the CAIP for language (spelling, grammar, sentence structure, etc.), markup (Github flavored Markdown), code style.

If the CAIP isn't ready, the editor will send it back to the author for revision, with specific instructions.

Once the CAIP is ready for the repository, the CAIP editor will:

- Assign a CAIP number (generally the PR number or, if preferred by the author, the Issue # if there was discussion in the Issues section of this repository about this CAIP)

- Merge the corresponding pull request

- Send a message back to the CAIP author with the next step.

The editors don't pass judgment on CAIPs. We merely do the administrative & editorial part.

## History

This document was derived heavily from [Bitcoin's BIP-0001] written by Amir Taaki, which in turn was derived from [Python's PEP-0001]. In many places text was simply copied and modified. Although the PEP-0001 text was written by Barry Warsaw, Jeremy Hylton, and David Goodger, they are not responsible for its use in Chain Agnostic Improvement Proposals, and should not be bothered with technical questions specific to CAIPs. Please direct all comments to the CAIP editors.

### Bibliography

[markdown]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
[Bitcoin's BIP-0001]: https://github.com/bitcoin/bips
[Python's PEP-0001]: https://www.python.org/dev/peps/

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
