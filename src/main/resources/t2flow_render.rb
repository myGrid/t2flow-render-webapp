require 'rubygems'
require 'tempfile'
require 'taverna-t2flow'

##
# A simple operation for rendering a file from .t2flow to SVG (or PNG, if an
# alternate format is selected)
def render_as_format(from, to, format="svg")
  thefile = File.new(from, "r")
  dotfile = Tempfile.new('dot')
  begin
    model = T2Flow::Parser.new.parse(thefile)
    T2Flow::Dot.new.write_dot(dotfile, model)
    dotfile.close
    `dot -T#{format} -o"#{to}" #{dotfile.path}`
  ensure
    dotfile.unlink
  end
end

render_as_format(ARGV[0], ARGV[1])
